package com.wiserate.repository;

import com.wiserate.models.Loan;
import com.wiserate.models.MUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // find all loans by user id efficiently
    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId")
    List<Loan> findByUserId(Long userId);

    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.amortizationSchedule WHERE l.id = :id")
    Loan findLoanWithAmortizationPayments(Long id);

}
