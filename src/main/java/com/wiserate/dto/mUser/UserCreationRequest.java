package com.wiserate.dto.mUser;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserCreationRequest {
    @NotNull
    private String username;
    @NotNull
    private String email;
    @NotNull
    private String password;

}
