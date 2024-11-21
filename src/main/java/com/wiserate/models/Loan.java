package com.wiserate.models;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanTypes loanType;                 // Home loan for now.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProvinceCA province;

    @Column(nullable = false)
    private String municipality;

    @Column(nullable = false)
    private Double totalLoanAmount;             // 11000/22000/55000 etc.

    @Column(nullable = false)
    private Double downPayment;                 // 1000/2000/5000 etc.

    @Column(nullable = false)
    private Double principal;                   // 10000/20000/50000 etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interestType;          // 1 = Fixed, 2 = Variable

    @Column(nullable = false)
    private Boolean isCompoundInterest = true;  // Compound or simple interest.

    @Column(nullable = false)
    private int compoundFrequency = 2;          // 1/2/4/12 etc.

    @Column(nullable = false)
    private Double annualInterestRate;          // 0.05/0.10/0.15 etc.

    @Column(nullable = false)
    private int loanTermMonths;               // 6/12/24/36 etc. How many months the loan is for.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;      // Number of payments per year.

    @Column(nullable = false)
    private boolean isNewHomeBuyer = false;

    @Column(nullable = false)
    private LocalDate startDate;                // 2021-01-01
    private LocalDate endDate;                  // CALCULATED

    // USER INPUT
    private Double insurancePremium     = 700.0;
    private Double lawyerFee            = 1000.0;
    private Double appraisalFee         = 300.0;
    private Double homeInspectionFee    = 500.0;
    private Double otherFees            = 0.0;
    private Double titleInsurance       = 0.0;

    // CALCULATED
    private Double MunicipalLandTransferTax;
    private Double ProvincialLandTransferTax;
    private Double landTransferTaxRebate;
    private Double provincialSalesTax;
    private Double propertyTax = 0.0;
    private Double cmhcInsurance = 0.0;

    private Double periodicPayment;
    private Double totalInterest;

    private Double cashToClose;

    private Double totalPayment;

    @Column(nullable = false)
    private Boolean isActive = false;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

//    private Double residualValue;       // For car loans: Residual value (if applicable).

    // user can have multiple loans
    // user linked via fk_user_id [i.e. user_id]
    // Cascade not needed here as we are not saving User object from Loan object
    // FetchType.LAZY is used to load the user object only when it is needed
    //      else .Eager is used to load the full user object every time
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="fk_user_id", nullable = false)
    private MUser user;
}
