package br.com.estapar.poc.api.dto;

public record AuthResponse(
        Long customerId,
        String name,
        String email,
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
