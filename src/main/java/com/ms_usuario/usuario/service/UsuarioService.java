package com.ms_usuario.usuario.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ms_usuario.usuario.entities.Usuario;
import com.ms_usuario.usuario.entities.dto.UsuarioDto;
import com.ms_usuario.usuario.repositories.UsuarioRepository;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    public UsuarioService(UsuarioRepository usuarioRepository, CognitoIdentityProviderClient cognitoClient) {
        this.usuarioRepository = usuarioRepository;
        this.cognitoClient = cognitoClient;
    }

    @Override
    @Transactional
    public Usuario crearUsuario(UsuarioDto usuariodto) {
        String rolAsignado = (usuariodto.getRol() != null && !usuariodto.getRol().isBlank())
                ? usuariodto.getRol()
                : "ROLE_USER";

        // 1. Crear usuario en AWS Cognito sin enviar correo con clave temporal
        AdminCreateUserRequest cognitoRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(usuariodto.getEmail())
                .userAttributes(
                        AttributeType.builder().name("email").value(usuariodto.getEmail()).build(),
                        AttributeType.builder().name("email_verified").value("true").build()
                )
                .messageAction(MessageActionType.SUPPRESS)
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .build();

        AdminCreateUserResponse cognitoResponse = cognitoClient.adminCreateUser(cognitoRequest);
        UserType cognitoUser = cognitoResponse.user();

        // 2. Extraer el UUID 'sub' generado por Cognito
        String sub = cognitoUser.attributes().stream()
                .filter(attr -> attr.name().equals("sub"))
                .findFirst()
                .map(AttributeType::value)
                .orElseThrow(() -> new RuntimeException("No se pudo obtener el sub de Cognito"));

        // 3. Fijar la contraseña del formulario como permanente para permitir login inmediato
        if (usuariodto.getPassword() != null && !usuariodto.getPassword().isBlank()) {
            AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(usuariodto.getEmail())
                    .password(usuariodto.getPassword())
                    .permanent(true)
                    .build();
            cognitoClient.adminSetUserPassword(passwordRequest);
        }

        // 4. Asignar usuario al Grupo correspondiente en Cognito
        AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .username(usuariodto.getEmail())
                .groupName(rolAsignado)
                .build();
        cognitoClient.adminAddUserToGroup(groupRequest);

        // 5. Guardar la entidad en PostgreSQL vinculando el cognito_sub como @Id
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setCognitoSub(sub);
        nuevoUsuario.setEmail(usuariodto.getEmail());
        nuevoUsuario.setRut(usuariodto.getRut());
        nuevoUsuario.setNombre(usuariodto.getNombre());
        nuevoUsuario.setApellido(usuariodto.getApellido());
        nuevoUsuario.setRol(rolAsignado);

        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario obtenerPorSub(String sub) {
        return usuarioRepository.findById(sub)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con sub: " + sub));
    }
}