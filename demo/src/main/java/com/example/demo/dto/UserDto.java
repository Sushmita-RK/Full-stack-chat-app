package com.example.demo.dto;

// A "record" is a modern, concise way to create an immutable DTO.
// This will be serialized to JSON: { "id": 1, "username": "tarun" }
public record UserDto(Long id, String username) {
}
