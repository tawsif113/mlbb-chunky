package com.mlbbchunky.auth.application;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MlbbIdentityProvider identityProvider;

    public AuthService(MlbbIdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public void sendVerificationCode(long roleId, long zoneId) {
        identityProvider.sendVerificationCode(roleId, zoneId);
    }

    public MlbbIdentityProvider.VerifiedMlbbProfile verify(
            long roleId,
            long zoneId,
            String verificationCode
    ) {
        return identityProvider.verify(roleId, zoneId, verificationCode);
    }
}
