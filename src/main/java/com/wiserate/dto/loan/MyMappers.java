package com.wiserate.dto.loan;


import com.wiserate.models.Loan;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import com.wiserate.dto.loan.NewLoanRequestData;
import com.wiserate.dto.loan.LoanResponseData;

@Mapper(componentModel = "spring")
public interface MyMappers {
    Loan mapObjectToLoan(@Valid NewLoanRequestData newLoanRequestData);

    LoanResponseData mapLoanToResponseData(Loan loan);
}
