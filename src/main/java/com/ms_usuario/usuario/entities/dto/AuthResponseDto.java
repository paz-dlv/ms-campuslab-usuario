package com.ms_usuario.usuario.entities.dto;

public record AuthResponseDto(
    String accessToken,
    String idToken,
    String refreshToken,
    Integer expiresIn,
    String tokenType
) {}