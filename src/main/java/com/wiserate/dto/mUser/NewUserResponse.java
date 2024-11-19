package com.wiserate.dto.mUser;

import com.wiserate.models.MUser;
import com.wiserate.enums.MUserRoles;
import lombok.Getter;

@Getter
public class NewUserResponse {
    private final Long id;
    private final String username;
    private final String email;
    private final MUserRoles role;


    public NewUserResponse(MUser mUser){
        this.id = mUser.getId();
        this.username = mUser.getUsername();
        this.role = mUser.getRole();
        this.email = mUser.getEmail();
    }
}
