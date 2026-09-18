package com.hackathon.payment.auth.dto;

public record LoginResponse(String accessToken, long expiresIn) {
}
