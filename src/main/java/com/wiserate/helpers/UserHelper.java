package com.wiserate.helpers;

import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.exceptions.mUser.UserNotFoundException;
import com.wiserate.models.MUser;
import com.wiserate.repository.MUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class UserHelper {

    private final MUserRepository mUserRepository;

    @Autowired
    public UserHelper(MUserRepository mUserRepository) {
        this.mUserRepository = mUserRepository;
    }

    public UserDTO validateUser(Authentication authentication) {
        String username = authentication.getName();
        MUser mUser = mUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return new UserDTO(mUser);
    }

    public String getUserRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public boolean isUserAdmin(Authentication authentication) {
        return getUserRole(authentication).equals("ROLE_ADMIN");
    }
}