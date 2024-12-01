package com.wiserate.controller;

import com.wiserate.dto.mUser.UserCreationRequest;
import com.wiserate.dto.mUser.UserDTO;
import com.wiserate.dto.mUser.UpdateRequest;
import com.wiserate.exceptions.mUser.ErrorResponse;
import com.wiserate.exceptions.mUser.UnauthorizedException;
import com.wiserate.models.MUser;
import com.wiserate.services.MUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// This allows the frontend running on http://localhost:3000 to communicate with this backend API.
// @CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/user")
public class UserController {

    private final MUserService mUserService;

    public UserController(MUserService mUserService) {
        this.mUserService = mUserService;
    }

    // LOGIN
    @GetMapping("/login")
    public ResponseEntity<?> login(Authentication authentication) {
        try {
            UserDTO user = mUserService.validateUser(authentication);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("Login failed", e.getMessage(), HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    // GET ALL USERS
    @GetMapping("/all")
    public ResponseEntity<?> getUsers(Authentication authentication) {
        try {
            return new ResponseEntity<>(mUserService.getMUsers(authentication), HttpStatus.OK);
        } catch (UnauthorizedException e) {
            ErrorResponse errorResponse = new ErrorResponse("Unauthorized", e.getMessage(), HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // GET USER BY USERNAME
    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username, Authentication authentication) {
        try {
            UserDTO user = mUserService.getMUser(username, authentication);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("User not found", e.getMessage(), HttpStatus.NOT_FOUND.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    // CREATE USER
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserCreationRequest userCreationRequest) {
        try {
            System.out.println(userCreationRequest.getUsername());
            MUser user = new MUser(userCreationRequest.getUsername(), userCreationRequest.getPassword(), userCreationRequest.getEmail());
            return new ResponseEntity<>(mUserService.createMUser(user), HttpStatus.CREATED);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("failed", e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // UPDATE USER
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateRequest updateRequest, Authentication authentication) {
        try {
            MUser user = mUserService.updateMUser(updateRequest, authentication);
            UserDTO userDTO = new UserDTO(user);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("User update failed", e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
