package com.wiserate.dto.loan;

import com.wiserate.models.CalculatedAmounts;
import com.wiserate.models.Fees;
import com.wiserate.models.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapToLoan {

    // Map NewLoanRequestData to Loan
    public Loan mapObject(NewLoanRequestData newLoanRequestData) {
        Loan loan = new Loan();

//        Fees fees = new Fees();
//        CalculatedAmounts calculatedAmounts = new CalculatedAmounts();

        loan.setProvince(newLoanRequestData.getProvince());
        loan.setLoanType(newLoanRequestData.getLoanType());
        loan.setMunicipality(newLoanRequestData.getMunicipality());
        loan.setTotalLoanAmount(newLoanRequestData.getTotalLoanAmount());
        loan.setDownPayment(newLoanRequestData.getDownPayment());
        loan.setInterestType(newLoanRequestData.getInterestType());
        loan.setIsCompoundInterest(newLoanRequestData.getIsCompoundInterest());
        loan.setCompoundFrequency(newLoanRequestData.getCompoundFrequency());
        loan.setAnnualInterestRate(newLoanRequestData.getAnnualInterestRate());
        loan.setLoanTermMonths(newLoanRequestData.getLoanTermMonths());
        loan.setPaymentFrequency(newLoanRequestData.getPaymentFrequency());
        loan.setNewHomeBuyer(newLoanRequestData.isNewHomeBuyer());

//        fees.setInsurancePremium(newLoanRequestData.getInsurancePremium());
//        fees.setLawyerFee(newLoanRequestData.getLawyerFee());
//        fees.setAppraisalFee(newLoanRequestData.getAppraisalFee());
//        fees.setHomeInspectionFee(newLoanRequestData.getHomeInspectionFee());
//        fees.setOtherFees(newLoanRequestData.getOtherFees());
//        fees.setTitleInsurance(newLoanRequestData.getTitleInsurance());

        loan.setFees(newLoanRequestData.getFees());

//        loan.setInsurancePremium(newLoanRequestData.getInsurancePremium());
//        loan.setLawyerFee(newLoanRequestData.getLawyerFee());
//        loan.setAppraisalFee(newLoanRequestData.getAppraisalFee());
//        loan.setHomeInspectionFee(newLoanRequestData.getHomeInspectionFee());
//        loan.setOtherFees(newLoanRequestData.getOtherFees());
//        loan.setTitleInsurance(newLoanRequestData.getTitleInsurance());

        loan.setIsActive(newLoanRequestData.getIsActive() != null ? newLoanRequestData.getIsActive() : true);

        return loan;
    }
}