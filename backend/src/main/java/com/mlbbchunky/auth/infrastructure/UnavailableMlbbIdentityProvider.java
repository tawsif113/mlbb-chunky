package com.mlbbchunky.auth.infrastructure;

import com.mlbbchunky.auth.application.MlbbIdentityProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@ConditionalOnMissingBean(MlbbIdentityProvider.class)
public class UnavailableMlbbIdentityProvider implements MlbbIdentityProvider {

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No MLBB identity provider is configured yet"
        );
    }

    @Override
    public void sendVerificationCode(long roleId, long zoneId) {
        throw unavailable();
    }

    @Override
    public VerifiedMlbbProfile verify(long roleId, long zoneId, String verificationCode) {
        throw unavailable();
    }
}
