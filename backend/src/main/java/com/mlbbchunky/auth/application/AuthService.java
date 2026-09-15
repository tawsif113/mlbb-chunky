package com.mlbbchunky.auth.application;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ObjectProvider<MlbbIdentityProvider> identityProviders;
    private final AuthRepository authRepository;
    private final long sessionDays;

    public AuthService(
            ObjectProvider<MlbbIdentityProvider> identityProviders,
            AuthRepository authRepository,
            @Value("${app.auth.session-days:30}") long sessionDays
    ) {
        this.identityProviders = identityProviders;
        this.authRepository = authRepository;
        if (sessionDays <= 0) {
            throw new IllegalArgumentException("app.auth.session-days must be positive");
        }
        this.sessionDays = sessionDays;
    }

    public void sendVerificationCode(long roleId, long zoneId) {
        provider().sendVerificationCode(roleId, zoneId);
    }

    @Transactional
    public Session verify(long roleId, long zoneId, String verificationCode) {
        MlbbIdentityProvider.VerifiedMlbbProfile profile = provider().verify(roleId, zoneId, verificationCode);
        AuthenticatedUser user = authRepository.upsertVerifiedUser(profile);

        String rawToken = newSessionToken();
        Instant expiresAt = Instant.now().plus(Duration.ofDays(sessionDays));
        authRepository.createSession(user.userId(), hashToken(rawToken), expiresAt);

        return new Session(rawToken, expiresAt, user);
    }

    public Optional<AuthenticatedUser> currentUser(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return authRepository.findUserByActiveSession(hashToken(rawToken), Instant.now());
    }

    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        authRepository.revokeSession(hashToken(rawToken));
    }

    private MlbbIdentityProvider provider() {
        MlbbIdentityProvider provider = identityProviders.getIfAvailable();
        if (provider == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No MLBB identity provider is configured yet"
            );
        }
        return provider;
    }

    private String newSessionToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    public record Session(String token, Instant expiresAt, AuthenticatedUser user) {}
}
