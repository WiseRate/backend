package com.wiserate.dto.loan;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.models.Fees;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class NewLoanRequestData {

    @NotNull
    private ProvinceCA province;

    @NotNull
    private LoanTypes loanType;

    @NotBlank
    private String municipality;

    @Positive
    private BigDecimal totalLoanAmount;

    @PositiveOrZero
    private BigDecimal downPayment;

    @NotNull
    private InterestType interestType;

    @NotNull
    private Boolean isCompoundInterest;

    @Min(1)
    private int compoundFrequency = 2;

    @Positive
    private BigDecimal annualInterestRate;

    @Min(1)
    private int loanTermMonths;

    @NotNull
    private PaymentFrequency paymentFrequency;

    @NotNull
    private boolean newHomeBuyer;

    @NotNull
    private Fees fees;

    private Boolean isActive;
}
