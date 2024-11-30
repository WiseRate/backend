package com.wiserate.dto.loan;

import com.wiserate.models.AmortizationPayment;
import com.wiserate.models.CalculatedAmounts;
import com.wiserate.models.Loan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponseData {
    private Long id;
    private BigDecimal periodicPayment;
    private BigDecimal cashToClose;
    private BigDecimal totalInterest;
    private BigDecimal totalPayment;
    private CalculatedAmounts calculatedAmounts;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<AmortizationPaymentDTO> amortizationSchedule;

    @Override
    public String toString() {
        return "LoanResponseData{" +
                "id=" + id +
                ", periodicPayment=" + periodicPayment +
                ", cashToClose=" + cashToClose +
                ", totalInterest=" + totalInterest +
                ", totalPayment=" + totalPayment +
                ", calculatedAmounts=" + calculatedAmounts +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    //    public LoanResponseData(Loan loan) {
//        this.id = loan.getId();
//        this.periodicPayment = loan.getPeriodicPayment();
//        this.cashToClose = loan.getCashToClose();
//        this.totalInterest = loan.getTotalInterest();
//        this.totalPayment = loan.getTotalPayment();
//        this.calculatedAmounts = loan.getCalculatedAmounts();
//    }
}
