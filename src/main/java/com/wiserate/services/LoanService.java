package com.wiserate.services;

import com.wiserate.models.Loan;
import com.wiserate.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    // get all user loans data [via user ID]
    public List<Loan> getLoansByUserId(Long id) {
        return loanRepository.findByUserId(id);
    }

    // get single loan data [via loan ID]
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id).orElse(null);
    }

    // add new loan
    public Loan addLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    // update loan

    // delete loan
    public int deleteLoan(Long id) {
        if (loanRepository.existsById(id)) {
            loanRepository.deleteById(id);
            return 1;
        }
        return 0;
    }

    // calculate loan

    // keep methods separate so they can be tested individually and used by other


}
