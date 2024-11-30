package com.wiserate.helpers;

import com.wiserate.enums.PaymentFrequency;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;

@Slf4j
@Component
public class LoanHelpers {

    // FREQUENCY TO PAYMENTS-PER-YEAR
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

    // PST = Provincial Sales Tax
    public BigDecimal calculatePST(BigDecimal cmhcAmount, String province) {
        log.debug("CALCULATING PROVINCIAL SALES TAX....");
        double rate = switch (province.toUpperCase()) {
            case "MB" -> 0.07;
            case "ON" -> 0.08;
            case "QC" -> 0.09;
            case "SK" -> 0.06;
            default -> 0.0;
        };
        return cmhcAmount.multiply(BigDecimal.valueOf(rate));
    }

    // REBATE
    public BigDecimal getMaxTaxRebate(String province) {
        log.debug("GETTING MAXIMUM TAX REBATE FOR PROVINCE: {}", province);
        return getMaxTaxRebate(province, "");
    }

    // REBATE OVERLOADED
    public BigDecimal getMaxTaxRebate(String province, String municipality) {
        log.debug("GETTING MAXIMUM TAX REBATE FOR PROVINCE: {} AND MUNICIPALITY: {}", province, municipality);
        return switch (province) {
            case "BC" -> BigDecimal.valueOf(8000.0);
            case "ON" -> {
                double provincialRebate = 4000.0;
                double municipalRebate = switch (municipality.toUpperCase()) {
                    case "TORONTO" -> 4475.0;
                    default -> 0.0;
                };
                yield BigDecimal.valueOf(provincialRebate + municipalRebate);
            }
            case "PE" -> BigDecimal.valueOf(2000.0);
            default -> BigDecimal.ZERO;
            // throw new IllegalArgumentException("Invalid province code: " + province);
        };
    }

    // CMHC Insurance
    public BigDecimal calculateCMHCPremium(BigDecimal totalLoanAmount, BigDecimal downPaymentPercentage) {
        log.debug("CALCULATING CMHC INSURANCE PREMIUM....");
        // Validate input
        log.debug("VALIDATING INPUT....");
        if (totalLoanAmount == null || totalLoanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total loan amount must be greater than zero.");
        }
        if (downPaymentPercentage == null ||
                downPaymentPercentage.compareTo(BigDecimal.ZERO) <= 0 ||
                downPaymentPercentage.compareTo(BigDecimal.valueOf(20)) > 0
        ) {
            throw new IllegalArgumentException("Down payment percentage must be between 0% and 20%.");
        }
        log.debug("INPUT VALIDATED....");

        log.debug("Loan Amount: {}", totalLoanAmount);
        log.debug("Down Payment Percentage: {}", downPaymentPercentage);

        NavigableMap<BigDecimal, BigDecimal> premiumRates = new TreeMap<>();
        premiumRates.put(BigDecimal.valueOf(5), new BigDecimal("0.04"));
        premiumRates.put(BigDecimal.valueOf(10), new BigDecimal("0.031"));
        premiumRates.put(BigDecimal.valueOf(15), new BigDecimal("0.028"));

        // ****  PREMIUM RATE  *****
        // floorEntry method of TreeMap:
        // FIRST get List of KEYs that are less than and equal >= SEARCHED KEY
        // Then returns the key with the highest value
        BigDecimal premiumRate = premiumRates.floorEntry(downPaymentPercentage).getValue();
        log.debug("Premium Rate: {}", premiumRate);

        // ****  PREMIUM  *****
        BigDecimal premium = totalLoanAmount.multiply(premiumRate);
        log.debug("Premium: {}", premium);

        premium = premium.setScale(2, RoundingMode.HALF_UP);
        return premium;
    }


    // Calculate end date based on start date and loan term
    public LocalDate calculateEndDate(@NotNull LocalDate startDate, int loanTermYears) {
        log.debug("CALCULATING END DATE....");
        return startDate.plusYears(loanTermYears);
    }


    // Calculate down payment percentage (optional utility)
    public BigDecimal calculateDownPaymentPercentage(BigDecimal downPayment, BigDecimal totalLoanAmount) {
        log.debug("CALCULATING DOWN PAYMENT PERCENTAGE....");
        log.debug("Down Payment: {}", downPayment);
        return downPayment.divide(totalLoanAmount, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

}
