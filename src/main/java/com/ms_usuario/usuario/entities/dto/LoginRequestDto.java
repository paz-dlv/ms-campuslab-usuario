package com.ms_usuario.usuario.entities.dto;

public record LoginRequestDto(
    String email,
    String password
) {}