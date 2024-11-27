package com.wiserate.models;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
@ToString
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
    private BigDecimal totalLoanAmount;             // 11000/22000/55000 etc.

    @Column(nullable = false)
    @PositiveOrZero
    private BigDecimal downPayment;                 // 1000/2000/5000 etc.

    @Column(nullable = false)
    private BigDecimal principal;                   // 10000/20000/50000 etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interestType;          // 1 = Fixed, 2 = Variable

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isCompoundInterest = true;  // Compound or simple interest.

    @Column(nullable = false)
    @Min(1)
    @Max(12)
    private int compoundFrequency = 2;          // 1/2/4/12 etc.

    @Column(nullable = false)
    private BigDecimal annualInterestRate;          // 0.05/0.10/0.15 etc.

    @Column(nullable = false)
    private int loanTermMonths;               // 6/12/24/36 etc. How many months the loan is for.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;      // Number of payments per year.

    @Column(nullable = false)
    private boolean newHomeBuyer = false;

    @CreatedDate
    @Column(nullable = false)
    private LocalDate startDate;                // 2021-01-01
    private LocalDate endDate;                  // CALCULATED

    // USER INPUT
    @Embedded
    private Fees fees;

    // CALCULATED
    @Embedded
    private CalculatedAmounts calculatedAmounts;

    //    @Transient    // USED WHEN NOT TO SAVE IN DB BUT WE ARE SAVING IT
    private BigDecimal periodicPayment;
    private BigDecimal totalInterest;

    private BigDecimal cashToClose;

    private BigDecimal totalPayment;

    @Column(nullable = false)
    private Boolean isActive = false;

    @UpdateTimestamp
    private LocalDateTime lastUpdated;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private MUser user;

    // CascadeType.ALL ensure that when we save a loan, it will save all the amortization payments as well.
    // orphanRemoval = true ensures that when we remove a loan, it will remove all the amortization payments as well.
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AmortizationPayment> amortizationSchedule = new ArrayList<>();


}


//    private BigDecimal residualValue;       // For car loans: Residual value (if applicable).

// user can have multiple loans
// user linked via fk_user_id [i.e. user_id]
// Cascade not needed here as we are not saving User object from Loan object
// FetchType.LAZY is used to load the user object only when it is needed
//      else .Eager is used to load the full user object every time

/*

@Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", loanType=" + loanType +
                ", province=" + province +
                ", municipality='" + municipality + '\'' +
                ", totalLoanAmount=" + totalLoanAmount +
                ", downPayment=" + downPayment +
                ", principal=" + principal +
                ", interestType=" + interestType +
                ", isCompoundInterest=" + isCompoundInterest +
                ", compoundFrequency=" + compoundFrequency +
                ", annualInterestRate=" + annualInterestRate +
                ", loanTermMonths=" + loanTermMonths +
                ", paymentFrequency=" + paymentFrequency +
                ", newHomeBuyer=" + newHomeBuyer +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", fees=" + fees +
                ", calculatedAmounts=" + calculatedAmounts +
                ", periodicPayment=" + periodicPayment +
                ", totalInterest=" + totalInterest +
                ", cashToClose=" + cashToClose +
                ", totalPayment=" + totalPayment +
                ", isActive=" + isActive +
                ", lastUpdated=" + lastUpdated +
                ", user=" + user +
                '}';
    }
 */