package com.ms_usuario.usuario.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import com.ms_usuario.usuario.entities.dto.AuthResponseDto;
import com.ms_usuario.usuario.entities.dto.LoginRequestDto;

import java.util.Map;

@Service
public class AuthService {

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.app-client-id}")
    private String clientId;

    public AuthService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public AuthResponseDto login(LoginRequestDto request) {
        Map<String, String> authParams = Map.of(
            "USERNAME", request.email(),
            "PASSWORD", request.password()
        );

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
            .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
            .clientId(clientId)
            .authParameters(authParams)
            .build();

        InitiateAuthResponse response = cognitoClient.initiateAuth(authRequest);
        AuthenticationResultType result = response.authenticationResult();

        return new AuthResponseDto(
            result.accessToken(),
            result.idToken(),
            result.refreshToken(),
            result.expiresIn(),
            result.tokenType()
        );
    }
}