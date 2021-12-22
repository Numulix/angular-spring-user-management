package com.raf.usermanagement.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.raf.usermanagement.models.User;
import com.raf.usermanagement.request.CreateUserRequest;
import com.raf.usermanagement.response.UserResponse;
import com.raf.usermanagement.services.UserService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findById(username);

        if (user.isPresent()) {
            User u = user.get();
            if (u.getPermission().getCanReadUser() == 1) {
                List<User> userList = userService.findAll();
                List<UserResponse> returnList = new ArrayList<>();
                for (User toAdd: userList) {
                    returnList.add(new UserResponse(toAdd.getName(), toAdd.getSurname(), toAdd.getEmail(), toAdd.getPermission()));
                }

                return ResponseEntity.ok(returnList);
            } else {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest toCreateUser) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findById(username);

        if (user.isPresent()) {
            User u = user.get();

            if (u.getPermission().getCanCreateUser() == 1) {

                Optional<User> existingUser = userService.findById(toCreateUser.getEmail());

                if (existingUser.isPresent()) {
                    return ResponseEntity.status(400).body("User with that email already exists");
                }

                User toAdd = new User(
                    toCreateUser.getEmail(),
                    toCreateUser.getName(),
                    toCreateUser.getSurname(),
                    this.passwordEncoder.encode(toCreateUser.getPassword()),
                    toCreateUser.getPermission()
                );

                userService.save(toAdd);

                UserResponse response = new UserResponse(toAdd.getName(), toAdd.getSurname(), toAdd.getEmail(), toAdd.getPermission());

                return ResponseEntity.ok(response);
            }

        } else {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.status(401).build();

    }

}
