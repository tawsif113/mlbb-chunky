package com.mlbbchunky.auth.application;

public interface MlbbIdentityProvider {

    void sendVerificationCode(long roleId, long zoneId);

    VerifiedMlbbProfile verify(long roleId, long zoneId, String verificationCode);

    record VerifiedMlbbProfile(
            long roleId,
            long zoneId,
            String nickname,
            String avatarUrl,
            Integer level,
            Integer rankLevel,
            Integer highestRankLevel,
            String registeredCountry
    ) {}
}
