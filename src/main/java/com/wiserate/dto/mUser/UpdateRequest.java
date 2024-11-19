package com.wiserate.dto.mUser;

import com.wiserate.enums.MUserRoles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class UpdateRequest {

    @NotNull(message = "UserID is required")
    private Long id;

    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotNull(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Role is required")
    private MUserRoles role;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}
