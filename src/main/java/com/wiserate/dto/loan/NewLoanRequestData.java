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
    @NotNull
    private String municipality;

    @Positive
    @NotNull
    private BigDecimal totalLoanAmount;

    @PositiveOrZero
    @NotNull
    private BigDecimal downPayment;

    @NotNull
    private InterestType interestType;

    @NotNull
    private Boolean isCompoundInterest;

    @Min(1)
    private Integer compoundFrequency = 2;

    @Positive
    @NotNull
    private BigDecimal annualInterestRate;

    @Min(1)
    @NotNull
    private Integer loanTermMonths;

    @NotNull
    private PaymentFrequency paymentFrequency;

    @NotNull
    private Boolean newHomeBuyer;

    @NotNull
    private Fees fees;

    private Boolean isActive;

    @Override
    public String toString() {
        return "NewLoanRequestData{" +
                "province=" + province +
                ", loanType=" + loanType +
                ", municipality='" + municipality + '\'' +
                ", totalLoanAmount=" + totalLoanAmount +
                ", downPayment=" + downPayment +
                ", interestType=" + interestType +
                ", isCompoundInterest=" + isCompoundInterest +
                ", compoundFrequency=" + compoundFrequency +
                ", annualInterestRate=" + annualInterestRate +
                ", loanTermMonths=" + loanTermMonths +
                ", paymentFrequency=" + paymentFrequency +
                ", newHomeBuyer=" + newHomeBuyer +
                ", fees=" + fees +
                ", isActive=" + isActive +
                '}';
    }
}
