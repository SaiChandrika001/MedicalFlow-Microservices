package com.medicalflow.userservice.service;

import com.medicalflow.userservice.dto.AuthResponse;
import com.medicalflow.userservice.dto.LoginRequest;
import com.medicalflow.userservice.dto.RegisterRequest;
import com.medicalflow.userservice.entity.Role;
import com.medicalflow.userservice.entity.User;
import com.medicalflow.userservice.exception.UserNotFoundException;
import com.medicalflow.userservice.repository.UserRepository;
import com.medicalflow.userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private JwtUtil jwtUtil = new JwtUtil("01234567890123456789012345678901", 3600000);

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, authenticationManager, jwtUtil);
    }

    @Test
    void authenticateReturnsAuthTokenWhenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password123");

        Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        doReturn(authentication).when(authenticationManager).authenticate(any(Authentication.class));

        AuthResponse response = userService.authenticate(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getAccessToken()).contains(".");
    }

    @Test
    void authenticateThrowsWhenCredentialsAreInvalid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("bad-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> userService.authenticate(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void registerCreatesNewUserWhenEmailDoesNotExist() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setFullName("New User");
        request.setPassword("secret");
        request.setRole("patient");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        assertThat(result.getFullName()).isEqualTo("New User");
        assertThat(result.getPassword()).isEqualTo("encoded-secret");
        assertThat(result.getRole()).isEqualTo(Role.PATIENT);
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setFullName("Existing User");
        request.setPassword("password");
        request.setRole("doctor");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void findByEmailReturnsUserWhenPresent() {
        User user = new User();
        user.setId(42L);
        user.setEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("user@example.com");

        assertThat(result).isSameAs(user);
    }

    @Test
    void findByEmailThrowsWhenMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email: missing@example.com");
    }

    @Test
    void findByIdReturnsUserWhenPresent() {
        User user = new User();
        user.setId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.of(user));

        User result = userService.findById(99L);

        assertThat(result).isSameAs(user);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(123L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 123");
    }
}
