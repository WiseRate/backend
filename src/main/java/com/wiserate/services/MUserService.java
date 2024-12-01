package com.wiserate.services;

import com.wiserate.dto.mUser.NewUserResponse;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.dto.mUser.UpdateRequest;
import com.wiserate.exceptions.mUser.*;
import com.wiserate.helpers.UserHelper;
import com.wiserate.models.MUser;
import com.wiserate.enums.MUserRoles;
import com.wiserate.repository.MUserRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MUserService {

    // private static final Logger log = LoggerFactory.getLogger(MUserService.class);
    private final MUserRepository mUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserHelper userHelper;


    @Autowired
    public MUserService(
            MUserRepository mUserRepository,
            BCryptPasswordEncoder passwordEncoder,
            UserHelper userHelper) {
        this.mUserRepository = mUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHelper = userHelper;
    }

    // (readOnly = true) Benefits:
    // 1. No Dirty Checks for

    // get user by ID
    @Transactional(readOnly = true)
    public MUser getUserById(Long id) {
        log.debug("GETTING USER BY ID: {}", id);
        return mUserRepository.findById(id).orElse(null);
    }


    // login validation
    @Transactional(readOnly = true)
    public UserDTO validateUser(Authentication authentication) {
        log.debug("VALIDATING USER: {}", authentication.getName());
        return userHelper.validateUser(authentication);
    }

    // get current userID from Authentication
    @Transactional(readOnly = true)
    public Long getCurrentUserId(Authentication authentication) {
        log.debug("GETTING CURRENT USER ID: {}", authentication.getName());
        return userHelper.validateUser(authentication).getId();
    }


    // GET ALL USERS ADMIN ONLY
    @Transactional(readOnly = true)
    public List<UserDTO> getMUsers(Authentication authentication) {
        log.debug("GETTING ALL USERS");
        userHelper.authorizeAdminAccess(authentication);
        List<MUser> users = mUserRepository.findAll();
        return users
                .stream()
                .map(user -> new UserDTO(user))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public UserDTO getMUser(String username, Authentication authentication) {
        log.debug("GETTING USER BY USERNAME: {}", username);
        // admin can get any user else only self
        userHelper.authorizeUserOrAdminAccess(username, authentication);
        MUser mUser = mUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return new UserDTO(mUser);
    }


    @Transactional
    public NewUserResponse createMUser(MUser mUser) {
        boolean isUserAlreadyExists = mUserRepository.findByUsername(mUser.getUsername()).isPresent();
        if (isUserAlreadyExists)
            throw new UserAlreadyExistsException("User already exists with username: " + mUser.getUsername());

        if (mUser.getRole() == null) mUser.setRole(MUserRoles.USER);
        mUser.setPassword(passwordEncoder.encode(mUser.getPassword()));

        try {
            MUser newUser = mUserRepository.save(mUser);
            return new NewUserResponse(newUser);
        } catch (Exception e) {
            System.out.println("ERROR WHILE SAVING USER: " + e.getMessage());
            throw new ErrorWhileSavingUserException("Error while saving user: " + e.getMessage());
        }
    }

    @Transactional
    public UserDTO updateUser(UpdateRequest updateRequest, Authentication authentication) {
        // call updateMUserAsAdmin if user is admin else default to updateMUser
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("UNAUTHORIZED REQUEST || USER NOT LOGGED IN");
            throw new UnauthorizedException("You are not authorized to perform this operation.");
        }
        MUser returnUser = null;
        if (userHelper.isUserAdmin(authentication)) {
            returnUser = updateMUserAsAdmin(updateRequest, authentication);
        } else {
            returnUser = updateMUser(updateRequest, authentication);
        }
        return new UserDTO(mUserRepository.findByIdWithUpdatedFields(returnUser.getId()));
    }

    private void checkAvailabilityAndUpdate(UpdateRequest updateRequest, MUser mUser) {
        checkAndUpdateUsername(updateRequest.getUsername(), mUser);
        checkAndUpdateEmail(updateRequest.getEmail(), mUser);
        checkAndUpdatePassword(updateRequest.getNewPassword(), mUser);
    }

    private void checkAndUpdateUsername(String newUsername, MUser mUser) {
        if (newUsername != null && !newUsername.equals(mUser.getUsername())) {
            log.debug("Checking if username is already taken");
            if (mUserRepository.findByUsername(newUsername).isPresent()) {
                log.error("USERNAME ALREADY TAKEN");
                throw new UserNameTakenException("Username already taken");
            }
            mUser.setUsername(newUsername);
        }
    }

    private void checkAndUpdateEmail(String newEmail, MUser mUser) {
        if (newEmail != null && !newEmail.equals(mUser.getEmail())) {
            log.debug("UPDATING EMAIL {}", newEmail);
            mUser.setEmail(newEmail);
        }
    }

    private void checkAndUpdatePassword(String newPassword, MUser mUser) {
        if (newPassword != null) {
            log.debug("UPDATING PASSWORD");
            mUser.setPassword(passwordEncoder.encode(newPassword));
        }
    }

    // ADMIN REQUEST
    public MUser updateMUserAsAdmin(UpdateRequest updateRequest, Authentication authentication) {
        log.debug("Admin Update Request for User ID: {}", updateRequest.getId());

        // Check if the current user is an admin
        if (!userHelper.isUserAdmin(authentication)) {
            log.error("UNAUTHORIZED REQUEST || USER IS NOT ADMIN");
            throw new UnauthorizedException("You are not authorized to perform this operation.");
        }

        // Check if the user exists
        MUser mUser = mUserRepository.findById(updateRequest.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update fields
        checkAvailabilityAndUpdate(updateRequest, mUser);

        // Update role [ADMIN]
        mUser.setRole(updateRequest.getRole());

        // Save and return updated user
        MUser updatedUser = mUserRepository.save(mUser);
        return updatedUser;
    }

    // USER REQUEST
    public MUser updateMUser(UpdateRequest updateRequest, Authentication authentication) {
        log.debug("User Update Request for User ID: {}", updateRequest.getId());

        // Ensure the user is updating their own profile
        MUser mUser = mUserRepository.findById(updateRequest.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!mUser.getUsername().equals(authentication.getName())) {
            throw new UnauthorizedException("You can only update your own profile.");
        }

        // Verify old password
        if (!passwordEncoder.matches(updateRequest.getPassword(), mUser.getPassword())) {
            throw new IncorrectPasswordException("Incorrect current password provided.");
        }

        // Update fields (except role)
        checkAvailabilityAndUpdate(updateRequest, mUser);

        // Save and return updated user
        MUser updatedUser = mUserRepository.save(mUser);
        return updatedUser;
    }


    /*
    public UserDTO updateMUser____OLD(UpdateRequest updateRequest, Authentication authentication) {

        System.out.println("USER UPDATE REQUEST: " + updateRequest.getId());

        String userRole = userHelper.getUserRole(authentication);
        boolean isCurrentUserAdmin = userHelper.isUserAdmin(authentication, userRole);

        // STEP 1: CHECKING IF USER ALREADY EXISTS
        Optional<MUser> isUserExist = mUserRepository.findById(updateRequest.getId());

        // STEP 1.1: IF USER NOT FOUND THROW EXCEPTION
        if (isUserExist.isEmpty()) throw new UserNotFoundException("User not found");

        // STEP 2: GETTING USER
        MUser mUser = isUserExist.get();

        if (isCurrentUserAdmin) mUser.setRole(updateRequest.getRole());

        // STEP 3: CHECKING IF OLD/CURRENT PASSWORD IS CORRECT
        if (!passwordEncoder.matches(updateRequest.getPassword(), mUser.getPassword())) {
            throw new IncorrectPasswordException("Password InValid");
        }

        String user_role = userHelper.getUserRole(authentication);
        log.debug("USER ROLE: {}", user_role);


        if (!updateRequest.getUsername().equals(mUser.getUsername())) {
            // CHECK IF NEW USERNAME IS ALREADY TAKEN
            Optional<MUser> isUsernameAlreadyTaken = mUserRepository.findByUsername(updateRequest.getUsername());
            // IF USERNAME IS ALREADY TAKEN THROW EXCEPTION
            if (isUsernameAlreadyTaken.isPresent()) throw new UserNameTakenException("Username already taken");
            // ELSE UPDATE USERNAME
            mUser.setUsername(updateRequest.getUsername());
        } else {
            mUser.setUsername(updateRequest.getUsername());
        }
        mUser.setEmail(updateRequest.getEmail());
        if (updateRequest.getNewPassword() != null) {
            mUser.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
        }
        try {
            MUser updatedUser = mUserRepository.save(mUser);
            return new UserDTO(updatedUser);
        } catch (Exception e) {
            throw new ErrorWhileSavingUserException("Error while updating user: " + e.getMessage());
        }

    }
    */
}
