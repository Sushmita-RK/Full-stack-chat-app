package com.example.demo.dto;

// Using 'record' for a concise, immutable DTO
public record RegisterRequest(String username, String password) {
}