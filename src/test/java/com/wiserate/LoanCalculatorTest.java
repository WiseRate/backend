package com.wiserate;

import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.helpers.LandTransferTax;
import com.wiserate.models.Fees;
import com.wiserate.models.Loan;
import com.wiserate.services.LoanCalculatorService;
import com.wiserate.services.LoanService;
import com.wiserate.services.PdfGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LoanCalculatorTest {

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private LoanService loanService;

    private Loan generateLoan() {
        return Loan.builder()
                .loanType(LoanTypes.HOME_LOAN)
                .province(ProvinceCA.ON)
                .municipality("toronto")
                .totalLoanAmount(BigDecimal.valueOf(200000.0))
                .downPayment(BigDecimal.valueOf(10000.0))
                .interestType(InterestType.VARIABLE)
                .isCompoundInterest(true)
                .compoundFrequency(2)
                .annualInterestRate(BigDecimal.valueOf(3.99))
                .loanTermMonths(300)
                .paymentFrequency(PaymentFrequency.MONTHLY)
                .newHomeBuyer(true)
                .startDate(LocalDate.of(2024, 12, 2))
                .fees(Fees.builder()
                        .insurancePremium(BigDecimal.valueOf(0.0))
                        .lawyerFee(BigDecimal.valueOf(1000.0))
                        .appraisalFee(BigDecimal.valueOf(300.0))
                        .homeInspectionFee(BigDecimal.valueOf(500.0))
                        .otherFees(BigDecimal.valueOf(0.0))
                        .titleInsurance(BigDecimal.valueOf(900.0))
                        .propertyTax(BigDecimal.valueOf(0.0))
                        .build())
                .isActive(true)
                .build();

    }



    // TESTING PDF GENERATION
    @Test
    public void testGeneratePdf() {
        Loan loan = generateLoan();
        loan = loanService.initializeLoan(loan);
        LoanResponseData responseData = loanService.convertToLoanResponseData(loan);
        System.out.println(responseData);
        byte[] pdf = pdfGeneratorService.generateAmortizationSchedulePdf("WiseRate", responseData);
        try(FileOutputStream file = new FileOutputStream("output.pdf")) {
            file.write(pdf);
        } catch (Exception e) {
            System.out.println("Error occurred while generating PDF under Method Name: "
                    + Thread.currentThread().getStackTrace()[1].getMethodName() + " : " + e);
        } finally {
            System.out.println("UNDER " + Thread.currentThread().getStackTrace()[1].getMethodName() + ": ");
            System.out.println("PDF generated successfully!");
        }
    }


}
