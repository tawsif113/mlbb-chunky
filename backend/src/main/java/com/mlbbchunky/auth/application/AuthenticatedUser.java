package com.mlbbchunky.auth.application;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        long roleId,
        long zoneId,
        String nickname,
        String avatarUrl,
        Integer level,
        Integer rankLevel,
        Integer highestRankLevel,
        String registeredCountry
) {}
