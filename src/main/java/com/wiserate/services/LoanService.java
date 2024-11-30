package com.wiserate.services;

import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.dto.loan.MyMappers;
import com.wiserate.dto.loan.NewLoanRequestData;
import com.wiserate.exceptions.wiseRate.ErrorInitializingLoanException;
import com.wiserate.models.Loan;
import com.wiserate.models.MUser;
import com.wiserate.repository.LoanRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LoanService {

    // private final Logger log = LoggerFactory.getLogger(LoanService.class);
    private final LoanRepository loanRepository;
    private final LoanCalculatorService loanCalculatorService;
    private final MyMappers myMappers;


    @Autowired
    public LoanService(LoanRepository loanRepository, LoanCalculatorService loanCalculatorService, MyMappers myMappers) {
        this.loanRepository = loanRepository;
        this.loanCalculatorService = loanCalculatorService;
        this.myMappers = myMappers;
    }

    // get all user loans data [via user ID]
    public List<Loan> getLoansByUserId(Long id) {
        return loanRepository.findByUserId(id);
    }

    // get single loan data [via loan ID]
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id).orElse(null);
    }

    // delete loan
    public int deleteLoan(Long id) {
        if (loanRepository.existsById(id)) {
            loanRepository.deleteById(id);
            return 1;
        }
        return 0;
    }

    // NOT-LOGIN
    public Loan initializeLoan(Loan loan) throws ErrorInitializingLoanException {
        log.debug("INITIALIZING LOAN....");
        try{
        loan = loanCalculatorService.initialize(loan);
            return loan;
        } catch (Exception e) {
            log.error("FAILED TO INITIALIZE LOAN....");
            throw new ErrorInitializingLoanException("Failed to initialize loan");
        }
    }

    // save/update loan
    public Loan saveLoan(Loan loan) {
        log.debug("SAVING LOAN TO DATABASE....");
        return loanRepository.save(loan);
    }

    // LOAN TO RESPONSE DATA
    public LoanResponseData convertToLoanResponseData(Loan loan) {
        log.debug("CONVERTING TO RESPONSE DATA....");
        log.debug("CHECKING amortizationSchedule DATA: {}", loan.getAmortizationSchedule().size());
        return myMappers.mapLoanToResponseData(loan);
    }

    // CLIENT DATA TO LOAN
    public Loan convertToLoan(NewLoanRequestData newLoanRequestData) {
        log.debug("CONVERTING TO LOAN OBJECT....");
        return myMappers.mapObjectToLoan(newLoanRequestData);
    }
}
