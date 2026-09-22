package com.ms_usuario.usuario.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ms_usuario.usuario.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}