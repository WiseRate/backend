package com.wiserate;

import com.wiserate.enums.InterestType;
import com.wiserate.enums.LoanTypes;
import com.wiserate.enums.PaymentFrequency;
import com.wiserate.enums.ProvinceCA;
import com.wiserate.helpers.LandTransferTax;
import com.wiserate.models.Loan;
import com.wiserate.services.LoanCalculatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LoanCalculatorTest {

    // Create an instance of the class that contains the function
    private final LoanCalculatorService loanCalculator;
    private final LandTransferTax landTransferTax;

    @Autowired
    public LoanCalculatorTest(LoanCalculatorService loanCalculator, LandTransferTax landTransferTax) {
        System.out.println("HERE 1");
        this.loanCalculator = loanCalculator;
        this.landTransferTax = landTransferTax;
        System.out.println("HERE 2");
    }

}
