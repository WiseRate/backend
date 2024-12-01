package com.wiserate.dto;


import com.wiserate.dto.mUser.NewUserResponse;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.models.Loan;
import com.wiserate.models.MUser;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import com.wiserate.dto.loan.NewLoanRequestData;
import com.wiserate.dto.loan.LoanResponseData;


@Mapper(componentModel = "spring")
public interface MyMappers {
    Loan mapObjectToLoan(@Valid NewLoanRequestData newLoanRequestData);

    LoanResponseData mapLoanToResponseData(Loan loan);

    NewUserResponse mapUserToNewUserResponse(MUser mUser);

    UserDTO mapUserToUserDTO(MUser mUser);
}
