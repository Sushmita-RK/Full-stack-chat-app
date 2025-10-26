package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal; // Import this
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(Principal principal) {
        // Get the username of the currently logged-in user
        String currentUsername = principal.getName();

        // Fetch all users from the database
        List<UserDto> users = userRepository.findAll()
                .stream()
                // --- ADDED FILTER ---
                // Filter out the currently logged-in user from the list
                .filter(user -> !user.getUsername().equals(currentUsername))
                // ---
                // Map each User object to a UserDto object
                .map(user -> new UserDto(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
        
        // Return the list as JSON
        return ResponseEntity.ok(users);
    }
}

