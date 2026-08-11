package com.microlend.loan.service;

import com.microlend.common.ApiException;
import com.microlend.loan.dto.EmiPreviewResponse;
import com.microlend.loan.dto.LoanProductRequest;
import com.microlend.loan.dto.LoanProductResponse;
import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.enums.ProductStatus;
import com.microlend.loan.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanProductServiceImpl implements LoanProductService {

    private final LoanProductRepository repository;
    private final EmiCalculationService emiService;

    @Override
    @Transactional
    public LoanProductResponse create(LoanProductRequest req) {
        validateAmounts(req);
        LoanProduct p = LoanProduct.builder()
                .productName(req.productName())
                .category(req.category())
                .minAmount(req.minAmount())
                .maxAmount(req.maxAmount())
                .tenureMonths(req.tenureMonths())
                .interestRatePercent(req.interestRatePercent())
                .interestType(req.interestType())
                .processingFeePercent(req.processingFeePercent())
                .status(req.status() == null ? ProductStatus.ACTIVE : req.status())
                .build();
        return LoanProductResponse.from(repository.save(p));
    }

    @Override
    @Transactional
    public LoanProductResponse update(Long id, LoanProductRequest req) {
        validateAmounts(req);
        LoanProduct p = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Loan product not found: " + id));
        p.setProductName(req.productName());
        p.setCategory(req.category());
        p.setMinAmount(req.minAmount());
        p.setMaxAmount(req.maxAmount());
        p.setTenureMonths(req.tenureMonths());
        p.setInterestRatePercent(req.interestRatePercent());
        p.setInterestType(req.interestType());
        p.setProcessingFeePercent(req.processingFeePercent());
        if (req.status() != null) {
            p.setStatus(req.status());
        }
        return LoanProductResponse.from(repository.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanProductResponse> findAll() {
        return repository.findAll().stream().map(LoanProductResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanProductResponse> findActive() {
        return repository.findByStatus(ProductStatus.ACTIVE).stream().map(LoanProductResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProductResponse findById(Long id) {
        return LoanProductResponse.from(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProduct getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Loan product not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmiPreviewResponse emiPreview(Long id) {
        LoanProduct p = getEntity(id);
        BigDecimal mid = p.getMinAmount().add(p.getMaxAmount())
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        return new EmiPreviewResponse(
                p.getProductId(),
                p.getMinAmount(), emiService.calculate(p.getMinAmount(), p.getInterestRatePercent(), p.getTenureMonths(), p.getInterestType()),
                mid, emiService.calculate(mid, p.getInterestRatePercent(), p.getTenureMonths(), p.getInterestType()),
                p.getMaxAmount(), emiService.calculate(p.getMaxAmount(), p.getInterestRatePercent(), p.getTenureMonths(), p.getInterestType()),
                p.getTenureMonths(), p.getInterestRatePercent(), p.getInterestType().name(),
                "Illustrative sample only — real EMI is recomputed on the actual sanctioned amount."
        );
    }

    private void validateAmounts(LoanProductRequest req) {
        if (req.maxAmount().compareTo(req.minAmount()) < 0) {
            throw ApiException.badRequest("maxAmount must be >= minAmount");
        }
    }
}
