package com.wiserate.helpers;

import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.exceptions.mUser.UnauthorizedException;
import com.wiserate.exceptions.mUser.UserNotFoundException;
import com.wiserate.models.MUser;
import com.wiserate.repository.MUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class UserHelper {

    private final MUserRepository mUserRepository;
    // private final Logger log = LoggerFactory.getLogger(UserHelper.class);

    @Autowired
    public UserHelper(MUserRepository mUserRepository) {
        this.mUserRepository = mUserRepository;
    }

    public UserDTO validateUser(Authentication authentication) {
        String username = authentication.getName();
        MUser mUser = mUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        getUserRole(authentication);
        return new UserDTO(mUser);
    }

    public String getUserRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        log.debug("USER ROLE: " + role);
        return role;
    }

    public boolean isUserAdmin(Authentication authentication) {
        return isUserAdmin(authentication, "");
    }

    public boolean isUserAdmin(Authentication authentication, String userRole) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (!userRole.isEmpty()){
            return userRole.equals("ROLE_ADMIN");
        }
        return getUserRole(authentication).equals("ROLE_ADMIN");
    }

    public void authorizeAdminAccess(Authentication authentication) {
        if (!isUserAdmin(authentication)) {
            log.error("UNAUTHORIZED ACCESS || USER IS NOT ADMIN");
            throw new UnauthorizedException("You are not authorized to access this resource.");
        }
    }

    public void authorizeUserOrAdminAccess(String username, Authentication authentication) {
        if (!isUserAdmin(authentication) && !username.equals(authentication.getName())) {
            log.error("UNAUTHORIZED ACCESS || USER IS NOT ADMIN OR OWNER");
            throw new UnauthorizedException("You are not authorized to access this resource.");
        }
    }
}