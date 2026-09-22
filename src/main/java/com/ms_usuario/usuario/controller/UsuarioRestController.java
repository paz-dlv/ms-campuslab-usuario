package com.ms_usuario.usuario.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ms_usuario.usuario.entities.Usuario;
import com.ms_usuario.usuario.entities.dto.UsuarioDto;
import com.ms_usuario.usuario.service.IUsuarioService;

@RestController 
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final IUsuarioService usuarioService;

    public UsuarioRestController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody UsuarioDto dto) {
        Usuario creado = usuarioService.crearUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{sub}")
    public ResponseEntity<Usuario> obtenerPorSub(@PathVariable String sub) {
        return ResponseEntity.ok(usuarioService.obtenerPorSub(sub));
    }
}