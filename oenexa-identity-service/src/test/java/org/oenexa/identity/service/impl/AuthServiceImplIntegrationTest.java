package org.oenexa.identity.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.common.exception.BusinessException;
import org.oenexa.common.exception.DuplicateResourceException;
import org.oenexa.identity.dto.request.LoginRequest;
import org.oenexa.identity.dto.request.RegisterRequest;
import org.oenexa.identity.dto.response.LoginResponse;
import org.oenexa.identity.dto.response.RegisterResponse;
import org.oenexa.identity.entity.UserEntity;
import org.oenexa.identity.repository.UserRepository;
import org.oenexa.identity.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("AuthServiceImpl BDD Integration Test Suite")
public class AuthServiceImplIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Given valid registration request, When register is called, Then new user is created in database")
    void shouldRegisterNewUserSuccessfully() {
        // Given: registration request
        RegisterRequest request = new RegisterRequest("Test", "User", "test.user@example.com", "Password123!");

        // When: register is called
        RegisterResponse response = authService.register(request);

        // Then: response has non-blank UUID and user exists in DB
        assertThat(response).isNotNull();
        assertThat(response.uuid()).isNotBlank();
        assertThat(userRepository.existsByEmail("test.user@example.com")).isTrue();
    }

    @Test
    @DisplayName("Given existing registered email, When registering duplicate, Then DuplicateResourceException is thrown")
    void shouldThrowWhenRegisteringDuplicateEmail() {
        // Given: existing user
        RegisterRequest request = new RegisterRequest("Test", "User", "test.user@example.com", "Password123!");
        authService.register(request);

        // When & Then: registering again throws DuplicateResourceException
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Given unverified registered user, When valid OTP is supplied, Then emailVerified is set to true")
    void shouldVerifyEmailSuccessfully() {
        // Given: registered user
        RegisterRequest request = new RegisterRequest("Test2", "User2", "test2.user@example.com", "Password123!");
        authService.register(request);

        // When: valid OTP verified
        authService.verifyEmail("test2.user@example.com", "123456");

        // Then: user entity has emailVerified true
        var user = userRepository.findByEmail("test2.user@example.com").orElseThrow();
        assertThat(user.getEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("Given non-existent email, When verifyEmail is called, Then BusinessException is thrown")
    void shouldThrowWhenVerifyingEmailNotFound() {
        // Given: non-existent email
        // When & Then: verifyEmail throws BusinessException
        assertThatThrownBy(() -> authService.verifyEmail("notfound@example.com", "123456"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Given already verified email, When verifyEmail is called again, Then BusinessException is thrown")
    void shouldThrowWhenEmailAlreadyVerified() {
        // Given: verified user
        RegisterRequest request = new RegisterRequest("Test", "User", "test@example.com", "Password123!");
        authService.register(request);
        authService.verifyEmail("test@example.com", "123456");

        // When & Then: verifying again throws BusinessException
        assertThatThrownBy(() -> authService.verifyEmail("test@example.com", "123456"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email is already verified");
    }

    @Test
    @DisplayName("Given invalid OTP code, When verifyEmail is called, Then BusinessException is thrown")
    void shouldThrowWhenOtpIsInvalid() {
        // Given: registered user
        RegisterRequest request = new RegisterRequest("Test", "User", "test@example.com", "Password123!");
        authService.register(request);

        // When & Then: wrong OTP throws BusinessException
        assertThatThrownBy(() -> authService.verifyEmail("test@example.com", "wrong"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    @Test
    @DisplayName("Given active user credentials, When login is executed, Then access and refresh JWT tokens are returned")
    void shouldLoginSuccessfully() {
        // Given: registered user
        RegisterRequest request = new RegisterRequest("Test3", "User3", "test3.user@example.com", "Password123!");
        authService.register(request);

        // When: login is executed
        var loginResponse = authService.login(new LoginRequest("test3.user@example.com", "Password123!"));

        // Then: valid tokens returned
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("Given non-existent email, When login is attempted, Then BusinessException is thrown")
    void shouldThrowWhenLoginUserNotFound() {
        // Given: unregistered credentials
        // When & Then: login throws BusinessException
        assertThatThrownBy(() -> authService.login(new LoginRequest("notfound@example.com", "Pass")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Given incorrect password, When login is attempted, Then BusinessException is thrown")
    void shouldThrowWhenLoginInvalidPassword() {
        // Given: registered user
        RegisterRequest request = new RegisterRequest("Test", "User", "test@example.com", "Password123!");
        authService.register(request);

        // When & Then: wrong password throws BusinessException
        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "WrongPass")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Given INACTIVE user account, When login is attempted, Then BusinessException is thrown")
    void shouldThrowWhenLoginInactiveAccount() {
        // Given: inactive user account
        RegisterRequest request = new RegisterRequest("Test", "User", "test@example.com", "Password123!");
        authService.register(request);
        UserEntity user = userRepository.findByEmail("test@example.com").orElseThrow();
        user.setAccountStatus("INACTIVE");
        userRepository.save(user);

        // When & Then: login throws BusinessException
        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "Password123!")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Account is not active");
    }

    @Test
    @DisplayName("Given secondary authentication stubs, When invoked, Then expected exceptions or completions occur")
    void shouldTestUnsupportedMethods() {
        // Given & When & Then: verify unsupported operations and logout handling
        assertThatThrownBy(() -> authService.verifyPhone("123", "123"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> authService.enableMfa("123"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> authService.verifyMfa("123", "123"))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> authService.refreshToken("invalid"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid refresh token");

        RegisterRequest request = new RegisterRequest("Test4", "User4", "test4.user@example.com", "Password123!");
        authService.register(request);
        var loginResponse = authService.login(new LoginRequest("test4.user@example.com", "Password123!"));
        String validToken = loginResponse.refreshToken();

        assertThatThrownBy(() -> authService.refreshToken(validToken))
                .isInstanceOf(UnsupportedOperationException.class);

        // Logout executes cleanly
        authService.logout("token");
    }
}
