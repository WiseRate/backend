package com.wiserate.dto.mUser;

import com.wiserate.models.MUser;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserDTO extends NewUserResponse {

    private LocalDateTime createdAt = null;
    private LocalDateTime modifiedAt = null;

    public UserDTO(MUser mUser){
        super(mUser);
        this.createdAt = mUser.getCreatedAt();
        this.modifiedAt = mUser.getModifiedAt();
    }
}
