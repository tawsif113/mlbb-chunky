package com.mlbbchunky.auth.application;

import com.mlbbchunky.auth.application.MlbbIdentityProvider.VerifiedMlbbProfile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthRepository {
    AuthenticatedUser upsertVerifiedUser(VerifiedMlbbProfile profile);
    void createSession(UUID userId, String tokenHash, Instant expiresAt);
    Optional<AuthenticatedUser> findUserByActiveSession(String tokenHash, Instant now);
    void revokeSession(String tokenHash);
}
