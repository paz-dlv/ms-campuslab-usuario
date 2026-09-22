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
        AdminCreateUserRequest cognitoRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(usuariodto.getEmail())
                .userAttributes(
                        AttributeType.builder().name("email").value(usuariodto.getEmail()).build(),
                        AttributeType.builder().name("email_verified").value("true").build()
                )
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .build();

        AdminCreateUserResponse cognitoResponse = cognitoClient.adminCreateUser(cognitoRequest);
        UserType cognitoUser = cognitoResponse.user();

        String sub = cognitoUser.attributes().stream()
                .filter(attr -> attr.name().equals("sub"))
                .findFirst()
                .map(AttributeType::value)
                .orElseThrow(() -> new RuntimeException("No se pudo obtener el sub de Cognito"));

        if (usuariodto.getRol() != null && !usuariodto.getRol().isEmpty()) {
            AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(usuariodto.getEmail())
                    .groupName(usuariodto.getRol())
                    .build();
            cognitoClient.adminAddUserToGroup(groupRequest);
        }

        Usuario nuevoUsuario = Usuario.builder()
                .cognitoSub(sub)
                .email(usuariodto.getEmail())
                .rut(usuariodto.getRut())
                .nombre(usuariodto.getNombre())
                .apellido(usuariodto.getApellido())
                .rol(usuariodto.getRol())
                .build();

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