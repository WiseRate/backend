package com.wiserate.services;

import com.wiserate.dto.loan.LoanResponseData;
import com.wiserate.models.Loan;
import com.wiserate.models.MUser;
import com.wiserate.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanCalculatorService loanCalculatorService;


    @Autowired
    public LoanService(LoanRepository loanRepository, LoanCalculatorService loanCalculatorService) {
        this.loanRepository = loanRepository;
        this.loanCalculatorService = loanCalculatorService;
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
    public Loan initializeLoan(Loan loan) {
        loan = loanCalculatorService.initialize(loan);
        return loan;
    }

    // save/update loan
    public LoanResponseData saveLoan(Loan loan) {
        Loan l = loanRepository.save(loan);
        LoanResponseData loanResponseData = new LoanResponseData(l);
        return loanResponseData;
    }


}
