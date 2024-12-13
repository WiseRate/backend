package com.wiserate.dto.mUser;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserCreationRequest {
    @NotNull
    @Size(min = 4, max = 50)
    private String username;
    @NotNull
    private String email;
    @NotNull
    @Size(min = 8, max = 50)
    private String password;

}
