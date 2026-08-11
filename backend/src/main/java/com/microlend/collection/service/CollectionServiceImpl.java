package com.microlend.collection.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.collection.dto.*;
import com.microlend.collection.entity.*;
import com.microlend.collection.enums.*;
import com.microlend.collection.repository.CollectionRecordRepository;
import com.microlend.collection.repository.CollectionReceiptRepository;
import com.microlend.common.ApiException;
import com.microlend.delinquency.service.DelinquencyService;
import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.ScheduleStatus;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import com.microlend.notification.enums.NotificationCategory;
import com.microlend.notification.service.NotificationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Collection entry + digital-receipt borrower-verification loop.

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRecordRepository recordRepository;
    private final CollectionReceiptRepository receiptRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final BorrowerRepository borrowerRepository;
    private final UserRepository userRepository;
    private final AuditGateway auditService;
    private final NotificationGateway notificationGateway;
    private final DelinquencyService delinquencyService;

    // ---------------- Record a collection (Field Officer) ----------------

    @Transactional
    public CollectionRecordResponse record(Long officerId, CollectionRequest req) {
        LoanAccount account = loanAccountRepository.findById(req.loanAccountId())
                .orElseThrow(() -> ApiException.notFound("Loan account not found: " + req.loanAccountId()));
        Borrower borrower = borrowerRepository.findById(account.getBorrowerId())
                .orElseThrow(() -> ApiException.notFound("Borrower not found"));

        // Field Officer may only collect against their own borrowers.
        if (!borrower.getRegisteredByFieldOfficerId().equals(officerId)) {
            throw ApiException.forbidden("Borrower not registered by this officer");
        }

        RepaymentSchedule schedule = scheduleRepository.findById(req.scheduleId())
                .orElseThrow(() -> ApiException.notFound("Schedule not found: " + req.scheduleId()));
        if (!schedule.getLoanAccountId().equals(account.getLoanAccountId())) {
            throw ApiException.badRequest("Schedule does not belong to the given loan account");
        }
        if (schedule.getStatus() == ScheduleStatus.PAID) {
            throw ApiException.badRequest("Installment already paid");
        }

        // Pending collection record.
        CollectionRecord recordEntity = CollectionRecord.builder()
                .loanAccountId(account.getLoanAccountId())
                .scheduleId(schedule.getScheduleId())
                .collectedAmount(req.collectedAmount())
                .collectionDate(LocalDate.now())
                .collectedById(officerId)
                .centreMeetingId(req.centreMeetingId())
                .mode(req.mode())
                .status(CollectionStatus.PENDING)
                .build();
        recordEntity = recordRepository.save(recordEntity);

        // Auto-generate the receipt (Pending borrower approval).
        CollectionReceipt receipt = CollectionReceipt.builder()
                .collectionId(recordEntity.getCollectionId())
                .loanAccountId(account.getLoanAccountId())
                .borrowerId(borrower.getBorrowerId())
                .fieldOfficerId(officerId)
                .statedAmount(req.collectedAmount())
                .collectionDate(recordEntity.getCollectionDate())
                .mode(req.mode())
                .borrowerApprovalStatus(BorrowerApprovalStatus.PENDING)
                .build();
        receipt = receiptRepository.save(receipt);

        auditService.record(officerId, "COLLECTION_RECORDED", "COLLECTION",
                "collectionId=" + recordEntity.getCollectionId() + " receiptId=" + receipt.getReceiptId()
                        + " amount=" + req.collectedAmount());

        // Notify the borrower's portal user to approve the receipt.
        if (borrower.getPortalUserId() != null) {
            notificationGateway.notifyUser(borrower.getPortalUserId(), Role.BORROWER.name(),
                    "A collection receipt of " + req.collectedAmount() + " awaits your approval",
                    NotificationCategory.RECEIPT, "CollectionReceipt", receipt.getReceiptId());
        }

        return CollectionRecordResponse.from(recordEntity, borrower.getBorrowerId(),
                borrower.getName(), receipt.getReceiptId());
    }

    @Transactional(readOnly = true)
    public List<CollectionRecordResponse> listForOfficer(Long officerId) {
        return recordRepository.findByCollectedById(officerId).stream()
                .map(r -> {
                    Long borrowerId = loanAccountRepository.findById(r.getLoanAccountId())
                            .map(LoanAccount::getBorrowerId).orElse(null);
                    String name = borrowerId == null ? "Unknown"
                            : borrowerRepository.findById(borrowerId).map(Borrower::getName).orElse("Unknown");
                    Long receiptId = receiptRepository.findByLoanAccountId(r.getLoanAccountId()).stream()
                            .filter(rc -> rc.getCollectionId().equals(r.getCollectionId()))
                            .map(CollectionReceipt::getReceiptId).findFirst().orElse(null);
                    return CollectionRecordResponse.from(r, borrowerId, name, receiptId);
                }).toList();
    }

    // ---------------- Borrower receipt inbox ----------------

    @Transactional(readOnly = true)
    public List<ReceiptResponse> borrowerReceipts(Long borrowerUserId, boolean pendingOnly) {
        Borrower borrower = borrowerRepository.findByPortalUserId(borrowerUserId)
                .orElseThrow(() -> ApiException.forbidden("No borrower profile for this user"));
        List<CollectionReceipt> receipts = pendingOnly
                ? receiptRepository.findByBorrowerIdAndBorrowerApprovalStatus(
                        borrower.getBorrowerId(), BorrowerApprovalStatus.PENDING)
                : receiptRepository.findByBorrowerId(borrower.getBorrowerId());
        return receipts.stream().map(r -> ReceiptResponse.from(r, borrower.getName())).toList();
    }

    @Transactional
    public ReceiptResponse approve(Long borrowerUserId, Long receiptId) {
        CollectionReceipt receipt = loadBorrowerReceipt(borrowerUserId, receiptId);
        applyApproval(receipt, BorrowerApprovalStatus.APPROVED, null, null);
        auditService.record(borrowerUserId, "RECEIPT_APPROVED", "COLLECTION", "receiptId=" + receiptId);
        Borrower borrower = borrowerRepository.findById(receipt.getBorrowerId()).orElseThrow();
        // Notify the field officer their collection was confirmed.
        notificationGateway.notifyUser(receipt.getFieldOfficerId(), Role.FIELD_OFFICER.name(),
                "Borrower " + borrower.getName() + " approved receipt " + receiptId,
                NotificationCategory.RECEIPT, "CollectionReceipt", receiptId);
        return ReceiptResponse.from(receipt, borrower.getName());
    }

    @Transactional
    public ReceiptResponse dispute(Long borrowerUserId, Long receiptId, String remarks) {
        CollectionReceipt receipt = loadBorrowerReceipt(borrowerUserId, receiptId);
        receipt.setBorrowerApprovalStatus(BorrowerApprovalStatus.DISPUTED);
        receipt.setDisputeRemarks(remarks);
        receiptRepository.save(receipt);
        // Mark the record disputed; DO NOT touch schedule/outstanding.
        recordRepository.findById(receipt.getCollectionId()).ifPresent(rec -> {
            rec.setStatus(CollectionStatus.DISPUTED);
            recordRepository.save(rec);
        });
        auditService.record(borrowerUserId, "RECEIPT_DISPUTED", "COLLECTION",
                "receiptId=" + receiptId + " remarks=" + remarks);
        Borrower borrower = borrowerRepository.findById(receipt.getBorrowerId()).orElseThrow();
        // Notify the branch manager of the borrower's branch for review.
        Long bmId = branchManagerFor(borrower);
        if (bmId != null) {
            notificationGateway.notifyUser(bmId, Role.BRANCH_MANAGER.name(),
                    "Receipt " + receiptId + " disputed by " + borrower.getName() + ": " + remarks,
                    NotificationCategory.RECEIPT, "CollectionReceipt", receiptId);
        }
        return ReceiptResponse.from(receipt, borrower.getName());
    }

    // ---------------- Branch Manager: disputes + co-sign ----------------

    @Transactional(readOnly = true)
    public List<ReceiptResponse> branchDisputes(Long managerUserId) {
        Long branchId = userRepository.findById(managerUserId).map(User::getBranchId).orElse(null);
        return receiptRepository.findByBorrowerApprovalStatus(BorrowerApprovalStatus.DISPUTED).stream()
                .filter(r -> branchId != null && branchId.equals(branchIdOfReceipt(r)))
                .map(r -> {
                    String name = borrowerRepository.findById(r.getBorrowerId())
                            .map(Borrower::getName).orElse("Unknown");
                    return ReceiptResponse.from(r, name);
                }).toList();
    }

    @Transactional
    public ReceiptResponse coSign(Long actorUserId, Long receiptId, String justification) {
        CollectionReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> ApiException.notFound("Receipt not found: " + receiptId));
        if (receipt.getBorrowerApprovalStatus() == BorrowerApprovalStatus.APPROVED
                || receipt.getBorrowerApprovalStatus() == BorrowerApprovalStatus.CO_SIGNED) {
            throw ApiException.badRequest("Receipt already approved");
        }
        applyApproval(receipt, BorrowerApprovalStatus.CO_SIGNED, actorUserId, justification);
        auditService.record(actorUserId, "RECEIPT_CO_SIGNED", "COLLECTION",
                "receiptId=" + receiptId + " justification=" + justification);
        String name = borrowerRepository.findById(receipt.getBorrowerId())
                .map(Borrower::getName).orElse("Unknown");
        return ReceiptResponse.from(receipt, name);
    }

    // ---------------- shared approval mechanics ----------------

    // Flips the receipt + record to confirmed, marks the installment Paid, reduces outstanding.
    private void applyApproval(CollectionReceipt receipt, BorrowerApprovalStatus status,
                               Long coSignerId, String justification) {
        receipt.setBorrowerApprovalStatus(status);
        receipt.setBorrowerApprovedDate(LocalDateTime.now());
        if (status == BorrowerApprovalStatus.CO_SIGNED) {
            receipt.setCoSignedById(coSignerId);
            receipt.setCoSignJustification(justification);
        }
        receiptRepository.save(receipt);

        recordRepository.findById(receipt.getCollectionId()).ifPresent(rec -> {
            rec.setStatus(CollectionStatus.CONFIRMED);
            recordRepository.save(rec);
        });

        RepaymentSchedule schedule = recordRepository.findById(receipt.getCollectionId())
                .flatMap(rec -> scheduleRepository.findById(rec.getScheduleId()))
                .orElseThrow(() -> ApiException.notFound("Schedule not found for receipt"));
        if (schedule.getStatus() != ScheduleStatus.PAID) {
            schedule.setStatus(ScheduleStatus.PAID);
            scheduleRepository.save(schedule);

            LoanAccount account = loanAccountRepository.findById(schedule.getLoanAccountId()).orElseThrow();
            BigDecimal newOutstanding = account.getOutstandingPrincipal().subtract(schedule.getPrincipalDue());
            if (newOutstanding.signum() < 0) {
                newOutstanding = BigDecimal.ZERO;
            }
            account.setOutstandingPrincipal(newOutstanding);
            loanAccountRepository.save(account);

            // clearing an installment may resolve an open delinquency case for this loan.
            delinquencyService.onLoanPaymentApplied(account.getLoanAccountId());
        }
    }

    private CollectionReceipt loadBorrowerReceipt(Long borrowerUserId, Long receiptId) {
        CollectionReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> ApiException.notFound("Receipt not found: " + receiptId));
        Borrower borrower = borrowerRepository.findById(receipt.getBorrowerId())
                .orElseThrow(() -> ApiException.notFound("Borrower not found"));
        if (!borrowerUserId.equals(borrower.getPortalUserId())) {
            throw ApiException.forbidden("This receipt does not belong to you");
        }
        if (receipt.getBorrowerApprovalStatus() != BorrowerApprovalStatus.PENDING) {
            throw ApiException.badRequest("Receipt is not pending (" + receipt.getBorrowerApprovalStatus() + ")");
        }
        return receipt;
    }

    private Long branchIdOfReceipt(CollectionReceipt r) {
        return borrowerRepository.findById(r.getBorrowerId())
                .map(this::branchOfBorrower).orElse(null);
    }

    private Long branchOfBorrower(Borrower b) {
        return userRepository.findById(b.getRegisteredByFieldOfficerId())
                .map(User::getBranchId).orElse(null);
    }

    private Long branchManagerFor(Borrower borrower) {
        Long branchId = branchOfBorrower(borrower);
        if (branchId == null) {
            return null;
        }
        return userRepository.findByBranchIdAndRole(branchId, Role.BRANCH_MANAGER).stream()
                .findFirst().map(User::getUserId).orElse(null);
    }
}
