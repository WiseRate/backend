package com.wiserate.dto.loan;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.models.CalculatedAmounts;
import com.wiserate.models.Fees;
import com.wiserate.models.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DemoLoan {

    public static Loan createDemoLoan() {
        Loan loan = new Loan();
        Fees fees = new Fees();
        CalculatedAmounts calculatedAmounts = new CalculatedAmounts();

        // Setting loan details
        loan.setLoanType(LoanTypes.HOME_LOAN);
        loan.setProvince(ProvinceCA.ON);
        loan.setTotalLoanAmount(new BigDecimal("1000000.00")); // Use BigDecimal for monetary values
        loan.setDownPayment(new BigDecimal("200000.00"));
        loan.setAnnualInterestRate(new BigDecimal("4.49")); // Interest rate in percentage
        loan.setInterestType(InterestType.VARIABLE);
        loan.setIsCompoundInterest(true);
        loan.setLoanTermMonths(300);
        loan.setPaymentFrequency(PaymentFrequency.MONTHLY);
        loan.setCompoundFrequency(2);
        loan.setNewHomeBuyer(true);
        loan.setMunicipality("Toronto");

        // Setting fees
        fees.setInsurancePremium(new BigDecimal("1000.00"));
        fees.setLawyerFee(new BigDecimal("1000.00"));
        fees.setAppraisalFee(new BigDecimal("1000.00"));
        fees.setHomeInspectionFee(new BigDecimal("1000.00"));
        fees.setOtherFees(new BigDecimal("1000.00"));
        fees.setTitleInsurance(new BigDecimal("1000.00"));
        loan.setFees(fees);

        // Calculated amounts placeholder
        loan.setCalculatedAmounts(calculatedAmounts);

        // Set start date
        loan.setStartDate(LocalDate.parse("2030-01-01"));

        return loan;
    }
}

