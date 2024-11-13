package pl.biketrack.auth;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.biketrack.email.EmailService;
import pl.biketrack.role.Role;
import pl.biketrack.role.RoleRepository;
import pl.biketrack.security.JwtService;
import pl.biketrack.user.Token;
import pl.biketrack.user.TokenRepository;
import pl.biketrack.user.User;
import pl.biketrack.user.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.nonNull;
import static pl.biketrack.email.EmailTemplateName.ACTIVATE_ACCOUNT;

@Slf4j
@Service
public class AuthenticationService {

    private static final String ROLE_USER = "USER";

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final String activationUrl;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, TokenRepository tokenRepository,
                                 EmailService emailService, @Value("${application.mailing.frontend.activation-url}") String activationUrl,
                                 AuthenticationManager authenticationManager, JwtService jwtService) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.activationUrl = activationUrl;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public ResponseEntity<Void> register(RegistrationRequest request) {
        validationOfExistingUser(request);

        User user = buildUser(request);
        userRepository.save(user);
        log.info("User registered");
        sendValidationEmail(user);

        return ResponseEntity.accepted()
                             .build();
    }

    private void validationOfExistingUser(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("User with e-mail {} already exists", request.getEmail());
            throw new EntityExistsException("Użytkownik o takim adresie e-mail już istnieje");
        }
    }

    private User buildUser(RegistrationRequest request) {
        Role userRole = roleRepository.findByName(ROLE_USER)
                                      .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono roli 'Użytkownik'"));

        return User.builder()
                   .nick(request.getNick())
                   .email(request.getEmail())
                   .password(passwordEncoder.encode(request.getPassword()))
                   .accountLocked(false)
                   .enabled(false)
                   .roles(List.of(userRole))
                   .build();
    }

    private void sendValidationEmail(User user) {
        String newToken = generateAndSaveActivationToken(user);
        emailService.sendEmail(user.getEmail(), user.getNick(), ACTIVATE_ACCOUNT, activationUrl, newToken, "Aktywacja konta");
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        Token token = buildToken(user, generatedToken);
        tokenRepository.save(token);
        return generatedToken;
    }

    private static Token buildToken(User user, String generatedToken) {
        return Token.builder()
                    .token(generatedToken)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now()
                                            .plusMinutes(15))
                    .user(user)
                    .build();
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        Authentication auth;

        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Niepoprawny email lub hasło");
        } catch (DisabledException | LockedException e) {
            throw new DisabledException("Konto nie jest aktywne");
        }

        User user = ((User) auth.getPrincipal());
        String jwtToken = jwtService.generateToken(user);

        log.info("User authenticated");
        return ResponseEntity.ok(new AuthenticationResponse(jwtToken));
    }

    public void activateAccount(String token) {
        Token savedToken = tokenRepository.findByToken(token)
                                          .orElseThrow(() -> {
                                              log.error("Token: {} not found", token);
                                              throw new EntityNotFoundException("Nie znaleziono tokenu");
                                          });

        if (nonNull(savedToken.getValidatedAt())) {
            log.error("Token: {} was used", token);
            throw new RuntimeException("Token został już użyty");
        }

        if (LocalDateTime.now()
                         .isAfter(savedToken.getExpiresAt())) {
            log.error("Token: {} expired", token);
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Token wygasł. Nowy token został wysłany na adres e-mail");
        }

        User user = userRepository.findByEmail(savedToken.getUser()
                                                         .getEmail())
                                  .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika"));
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
        log.info("User activated");
    }
}