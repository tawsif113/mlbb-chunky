package com.mlbbchunky.auth.application;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final ObjectProvider<MlbbIdentityProvider> identityProviders;

    public AuthService(ObjectProvider<MlbbIdentityProvider> identityProviders) {
        this.identityProviders = identityProviders;
    }

    public void sendVerificationCode(long roleId, long zoneId) {
        provider().sendVerificationCode(roleId, zoneId);
    }

    public MlbbIdentityProvider.VerifiedMlbbProfile verify(
            long roleId,
            long zoneId,
            String verificationCode
    ) {
        return provider().verify(roleId, zoneId, verificationCode);
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
}
