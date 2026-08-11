package com.microlend.loan.service;

import com.microlend.common.ApiException;
import com.microlend.loan.dto.LoanProductRequest;
import com.microlend.loan.dto.LoanProductResponse;
import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.enums.InterestType;
import com.microlend.loan.enums.LoanCategory;
import com.microlend.loan.enums.ProductStatus;
import com.microlend.loan.repository.LoanProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanProductServiceImplTest {

    @Mock
    LoanProductRepository repository;
    @Mock
    EmiCalculationService emiService;
    @InjectMocks
    LoanProductServiceImpl service;

    private LoanProductRequest req(String min, String max) {
        return new LoanProductRequest("Livelihood", LoanCategory.INDIVIDUAL,
                new BigDecimal(min), new BigDecimal(max), 12, new BigDecimal("18"),
                InterestType.REDUCING_BALANCE, new BigDecimal("1"), ProductStatus.ACTIVE);
    }

    @Test
    void createRejectsMaxLessThanMin() {
        assertThatThrownBy(() -> service.create(req("100000", "10000")))
                .isInstanceOf(ApiException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void createPersistsProduct() {
        when(repository.save(any(LoanProduct.class))).thenAnswer(inv -> inv.getArgument(0));
        LoanProductResponse res = service.create(req("10000", "100000"));
        assertThat(res.productName()).isEqualTo("Livelihood");
        assertThat(res.category()).isEqualTo(LoanCategory.INDIVIDUAL);
        verify(repository).save(any(LoanProduct.class));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ApiException.class);
    }
}
