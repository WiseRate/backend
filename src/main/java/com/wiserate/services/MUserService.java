package com.wiserate.services;

import com.wiserate.dto.mUser.NewUserResponse;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.dto.mUser.UpdateRequest;
import com.wiserate.exceptions.mUser.*;
import com.wiserate.helpers.UserHelper;
import com.wiserate.models.MUser;
import com.wiserate.enums.MUserRoles;
import com.wiserate.repository.MUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MUserService {

    private final MUserRepository mUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserHelper userHelper;


    @Autowired
    public MUserService(MUserRepository mUserRepository, BCryptPasswordEncoder passwordEncoder, UserHelper userHelper) {
        this.mUserRepository = mUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHelper = userHelper;
    }


    // login validation
    public UserDTO validateUser(Authentication authentication) {
        return userHelper.validateUser(authentication);
    }


    // GET ALL USERS ADMIN ONLY
    public List<UserDTO> getMUsers(Authentication authentication) {
        if (!userHelper.isUserAdmin(authentication))
            throw new UnauthorizedException("You are not authorized to access this resource.");
        List<MUser> users = mUserRepository.findAll();
        return users
                .stream()
                .map(user -> new UserDTO(user))
                .collect(Collectors.toList());
    }


    public UserDTO getMUser(String username, Authentication authentication) {
        // admin can get any user else only self
        if (!userHelper.isUserAdmin(authentication) && !username.equals(authentication.getName())) {
            // throw exception Unauthorized
            throw new UnauthorizedException("You are not authorized to access this resource.");
        }
        MUser mUser = mUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return new UserDTO(mUser);
    }


    public NewUserResponse createMUser(MUser mUser) {
        boolean isUserAlreadyExists = mUserRepository.findByUsername(mUser.getUsername()).isPresent();
        if (isUserAlreadyExists)
            throw new UserAlreadyExistsException("User already exists with username: " + mUser.getUsername());

        if (mUser.getRole() == null) mUser.setRole(MUserRoles.USER);
        mUser.setPassword(passwordEncoder.encode(mUser.getPassword()));

        try {
            System.out.println("USERNAME: " + mUser.getUsername() + "\tPASSWORD: " + mUser.getPassword() + "\tROLE: " + mUser.getRole());
            System.out.println("TRYING TO SAVE USER");
            MUser newUser = mUserRepository.save(mUser);
            System.out.println("USER SAVED SUCCESSFULLY");
            return new NewUserResponse(newUser);
        } catch (Exception e) {
            System.out.println("ERROR WHILE SAVING USER: " + e.getMessage());
            throw new ErrorWhileSavingUserException("Error while saving user: " + e.getMessage());
        }
    }


    public UserDTO updateMUser(UpdateRequest updateRequest, Authentication authentication) {

        System.out.println("USER UPDATE REQUEST: " + updateRequest.getId());

        // CHECKING IF USER EXISTS
        Optional<MUser> isUserExist = mUserRepository.findById(updateRequest.getId());


        if (isUserExist.isEmpty()) throw new UserNotFoundException("User not found");

        // VERIFY USER WITH ID and PASSWORD
        MUser mUser = isUserExist.get();

        if (!passwordEncoder.matches(updateRequest.getPassword(), mUser.getPassword())) {
            throw new IncorrectPasswordException("Password InValid");
        }

        String user_role = userHelper.getUserRole(authentication);
        System.out.println("USER ROLE: " + user_role);

        if (user_role.equals("ROLE_ADMIN")) mUser.setRole(updateRequest.getRole());

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
}
