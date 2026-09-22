package com.ms_usuario.usuario.service;

import com.ms_usuario.usuario.entities.Usuario;
import com.ms_usuario.usuario.entities.dto.UsuarioDto;
import java.util.List;

public interface IUsuarioService {

    Usuario crearUsuario(UsuarioDto usuarioDto);

    List<Usuario> obtenerTodos();

    Usuario obtenerPorSub(String sub);

}