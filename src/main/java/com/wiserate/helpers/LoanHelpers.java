package com.wiserate.helpers;

import com.wiserate.enums.PaymentFrequency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoanHelpers {


    public int noOfPaymentsPerYear(PaymentFrequency frequency) {
        log.debug("Calculating payments per year for frequency: {}", frequency);
        return switch (frequency) {
            case DAILY -> 365;
            case WEEKLY -> 52;
            case BIWEEKLY -> 26;
            case MONTHLY -> 12;
            case QUARTERLY -> 4;
            case SEMIANNUALLY -> 2;
            case ANNUALLY -> 1;
        };
    }
}
