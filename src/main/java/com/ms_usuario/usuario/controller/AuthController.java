package com.ms_usuario.usuario.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ms_usuario.usuario.entities.dto.AuthResponseDto;
import com.ms_usuario.usuario.entities.dto.LoginRequestDto;
import com.ms_usuario.usuario.service.AuthService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}