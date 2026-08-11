package com.microlend.borrower.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.dto.BorrowerRegistrationRequest;
import com.microlend.borrower.dto.BorrowerRegistrationResponse;
import com.microlend.borrower.dto.BorrowerResponse;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.enums.BorrowerStatus;
import com.microlend.borrower.enums.BorrowerType;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.common.VerhoeffValidator;
import com.microlend.grouporigination.entity.BorrowerGroup;
import com.microlend.grouporigination.entity.Centre;
import com.microlend.grouporigination.service.BorrowerGroupService;
import com.microlend.grouporigination.service.CentreService;
import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.UserStatus;
import com.microlend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.microlend.borrower.entity.BorrowerKYC;
import com.microlend.borrower.enums.KycStatus;
import com.microlend.borrower.repository.BorrowerKYCRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowerServiceImpl implements BorrowerService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final BorrowerRepository borrowerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CentreService centreService;
    private final BorrowerGroupService groupService;
    private final AuditGateway auditService;
    private final BorrowerKYCRepository borrowerKYCRepository;

    @Transactional
    public BorrowerRegistrationResponse register(Long officerId, BorrowerRegistrationRequest req) {
        // Dynamic age validation: Ensure borrower is at least 18 years old as of today
        if (req.dateOfBirth() == null) {
            throw ApiException.badRequest("Date of birth is required");
        }
        LocalDate today = LocalDate.now();
        int age = Period.between(req.dateOfBirth(), today).getYears();
        if (age < 18) {
            throw ApiException.badRequest("Borrower must be at least 18 years old to register for a loan");
        }

        // Verhoeff checksum on Aadhaar/National ID.
        if (!VerhoeffValidator.isValid(req.nationalIdNumber())) {
            throw ApiException.badRequest("National ID (Aadhaar) failed Verhoeff checksum validation");
        }
        if (borrowerRepository.existsByNationalIdNumber(req.nationalIdNumber())) {
            throw ApiException.conflict("A borrower with this National ID already exists");
        }
        if (borrowerRepository.existsByPhone(req.phone())) {
            throw ApiException.conflict("A borrower with this phone already exists");
        }
        if (userRepository.existsByEmail(req.portalEmail())) {
            throw ApiException.conflict("Portal email already in use");
        }
        if (userRepository.existsByPhone(req.phone())) {
            throw ApiException.conflict("A user with this phone already exists");
        }

        // Resolve centre / group per borrower type.
        Long centreId;
        Long groupId = null;
        Long branchId;
        if (req.borrowerType() == BorrowerType.INDIVIDUAL) {
            if (req.centreId() == null) {
                throw ApiException.badRequest("Individual borrower requires a centreId");
            }
            if (req.groupId() != null) {
                throw ApiException.badRequest("Individual borrower must not have a groupId");
            }
            Centre centre = centreService.getOwnedCentre(officerId, req.centreId());
            centreId = centre.getCentreId();
            branchId = centre.getBranchId();
        } else { // GROUP
            if (req.groupId() == null) {
                throw ApiException.badRequest("Group borrower requires a groupId");
            }
            BorrowerGroup group = groupService.getOwnedGroup(officerId, req.groupId());
            groupId = group.getGroupId();
            // Centre auto-derived from the group — not accepted from the client.
            Centre centre = centreService.getOwnedCentre(officerId, group.getCentreId());
            centreId = centre.getCentreId();
            branchId = centre.getBranchId();
        }

        // Auto-provision the portal User (Borrower role, default password, forced reset).
        String defaultPassword = generateDefaultPassword();
        User portalUser = User.builder()
                .name(req.name())
                .email(req.portalEmail())
                .phone(req.phone())
                .role(Role.BORROWER)
                .branchId(branchId)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .status(UserStatus.ACTIVE)
                .mustResetPassword(true)
                .build();
        portalUser = userRepository.save(portalUser);

        // Create the Borrower linked to the portal user.
        Borrower borrower = Borrower.builder()
                .name(req.name())
                .dateOfBirth(req.dateOfBirth())
                .gender(req.gender())
                .nationalIdNumber(req.nationalIdNumber())
                .village(req.village())
                .district(req.district())
                .phone(req.phone())
                .occupation(req.occupation())
                .monthlyIncome(req.monthlyIncome())
                .bankAccountNumber(req.bankAccountNumber())
                .ifscCode(req.ifscCode())
                .status(BorrowerStatus.ACTIVE)
                .registeredByFieldOfficerId(officerId)
                .borrowerType(req.borrowerType())
                .centreId(centreId)
                .groupId(groupId)
                .portalUserId(portalUser.getUserId())
                .build();
        borrower = borrowerRepository.save(borrower);

        if (groupId != null) {
            groupService.refreshMemberCount(groupId);
        }

        // Audit — never log the National ID or password unmasked.
        auditService.record(officerId, "BORROWER_REGISTERED", "BORROWER",
                "borrowerId=" + borrower.getBorrowerId() + " portalUserId=" + portalUser.getUserId());

        BorrowerResponse view = BorrowerResponse.from(borrower, officerName(officerId));
        return new BorrowerRegistrationResponse(view, portalUser.getUserId(), req.portalEmail(),
                defaultPassword,
                "Borrower registered. Portal account created — first login forces a password reset.");
    }

    @Transactional(readOnly = true)
    public List<BorrowerResponse> listForOfficer(Long officerId) {
        String name = officerName(officerId);
        return borrowerRepository.findByRegisteredByFieldOfficerId(officerId).stream()
                .map(b -> BorrowerResponse.from(b, name)).toList();
    }

    @Transactional(readOnly = true)
    public BorrowerResponse getForOfficer(Long officerId, Long borrowerId) {
        Borrower b = getOwned(officerId, borrowerId);
        return BorrowerResponse.from(b, officerName(officerId));
    }


    @Transactional(readOnly = true)
    public List<BorrowerResponse> listForBranch(Long managerUserId) {
        Long branchId = userRepository.findById(managerUserId).map(User::getBranchId).orElse(null);
        if (branchId == null) {
            return List.of();
        }
        List<User> officers = userRepository.findByBranchIdAndRole(branchId, Role.FIELD_OFFICER);
        return officers.stream()
                .flatMap(officer -> borrowerRepository
                        .findByRegisteredByFieldOfficerId(officer.getUserId()).stream()
                        .map(b -> BorrowerResponse.from(b, officer.getName())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Borrower getOwned(Long officerId, Long borrowerId) {
        Borrower b = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> ApiException.notFound("Borrower not found: " + borrowerId));
        if (!b.getRegisteredByFieldOfficerId().equals(officerId)) {
            throw ApiException.forbidden("Borrower not registered by this officer");
        }
        return b;
    }


    @Transactional(readOnly = true)
    public BorrowerResponse getByIdPrivileged(Long borrowerId) {
        Borrower b = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> ApiException.notFound("Borrower not found: " + borrowerId));
        return BorrowerResponse.from(b, officerName(b.getRegisteredByFieldOfficerId()));
    }

    public String officerName(Long officerId) {
        return userRepository.findById(officerId).map(User::getName).orElse("Unknown");
    }

    private String generateDefaultPassword() {
        return "Password@123";
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> pendingKycBorrowers() {

        return borrowerKYCRepository.findAll().stream()
                .filter(kyc -> kyc.getStatus() == KycStatus.PENDING)
                .map(BorrowerKYC::getBorrowerId)
                .distinct()
                .map(id -> borrowerRepository.findById(id).orElse(null))
                .filter(b -> b != null)
                .map(b -> Map.<String, Object>of(
                        "borrowerId", b.getBorrowerId(),
                        "name", b.getName()
                ))
                .toList();
    }
}