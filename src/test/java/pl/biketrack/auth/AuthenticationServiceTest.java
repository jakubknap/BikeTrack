package pl.biketrack.auth;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.biketrack.email.EmailService;
import pl.biketrack.role.Role;
import pl.biketrack.role.RoleRepository;
import pl.biketrack.security.JwtService;
import pl.biketrack.user.Token;
import pl.biketrack.user.TokenRepository;
import pl.biketrack.user.User;
import pl.biketrack.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegistrationRequest request = new RegistrationRequest("TestUser", "test@example.com", "password123");
        User user = new User();
        Role userRole = Role.builder()
                            .name("USER")
                            .build();

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        // When
        ResponseEntity<Void> response = authenticationService.register(request);

        // Then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password123");
        Authentication auth = new UsernamePasswordAuthenticationToken(new User(), null, List.of());

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken123");

        // When
        ResponseEntity<AuthenticationResponse> response = authenticationService.authenticate(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwtToken123",
                     Objects.requireNonNull(response.getBody())
                            .token());
        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationFails() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest("wrong@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Niepoprawny email lub hasło"));

        // When / Then
        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void shouldActivateAccountSuccessfully() {
        // Given
        String token = "valid-token";
        User user = User.builder()
                        .email("test@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .nick("TestUser")
                        .build();

        Token savedToken = Token.builder()
                                .token(token)
                                .expiresAt(LocalDateTime.now()
                                                        .plusHours(1))
                                .user(user)
                                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(savedToken));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenRepository.save(any(Token.class))).thenReturn(savedToken);

        // When
        authenticationService.activateAccount(token);

        // Then
        assertTrue(user.isEnabled());
        verify(userRepository, times(1)).save(user);
        assertNotNull(savedToken.getValidatedAt());
        verify(tokenRepository, times(1)).save(savedToken);
    }

    @Test
    void shouldThrowExceptionWhenTokenNotFound() {
        // Given
        String token = "invalid-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When / Then
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> authenticationService.activateAccount(token));
        assertEquals("Nie znaleziono tokenu", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTokenAlreadyUsed() {
        // Given
        String token = "used-token";
        User user = User.builder()
                        .email("test@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .nick("TestUser")
                        .build();

        Token savedToken = Token.builder()
                                .token(token)
                                .expiresAt(LocalDateTime.now()
                                                        .plusHours(1))
                                .user(user)
                                .build();
        savedToken.setValidatedAt(LocalDateTime.now());

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(savedToken));

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> authenticationService.activateAccount(token));
        assertEquals("Token został już użyty", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTokenExpired() {
        // Given
        String token = "expired-token";
        User user = User.builder()
                        .email("test@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .nick("TestUser")
                        .build();

        Token expiredToken = Token.builder()
                                  .token(token)
                                  .expiresAt(LocalDateTime.now()
                                                          .minusHours(1))
                                  .user(user)
                                  .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> authenticationService.activateAccount(token));
        assertEquals("Token wygasł. Nowy token został wysłany na adres e-mail", thrown.getMessage());
    }

}