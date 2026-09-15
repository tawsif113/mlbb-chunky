package com.mlbbchunky.auth.application;

import com.mlbbchunky.auth.application.MlbbIdentityProvider.VerifiedMlbbProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void verification_persists_user_and_stores_only_a_hash_of_the_session_token() {
        ObjectProvider<MlbbIdentityProvider> providers = mock(ObjectProvider.class);
        MlbbIdentityProvider provider = mock(MlbbIdentityProvider.class);
        AuthRepository repository = mock(AuthRepository.class);

        VerifiedMlbbProfile profile = new VerifiedMlbbProfile(
                123456789L,
                1234L,
                "Player",
                "https://example.test/avatar.jpg",
                100,
                200,
                250,
                "BD"
        );
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                profile.roleId(),
                profile.zoneId(),
                profile.nickname(),
                profile.avatarUrl(),
                profile.level(),
                profile.rankLevel(),
                profile.highestRankLevel(),
                profile.registeredCountry()
        );

        when(providers.getIfAvailable()).thenReturn(provider);
        when(provider.verify(profile.roleId(), profile.zoneId(), "7545")).thenReturn(profile);
        when(repository.upsertVerifiedUser(profile)).thenReturn(user);

        AuthService.Session session = new AuthService(providers, repository, 30)
                .verify(profile.roleId(), profile.zoneId(), "7545");

        assertThat(session.user()).isEqualTo(user);
        assertThat(session.token()).isNotBlank();
        assertThat(session.expiresAt()).isAfter(Instant.now());

        var tokenHashCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repository).createSession(eq(user.userId()), tokenHashCaptor.capture(), any(Instant.class));
        assertThat(tokenHashCaptor.getValue()).hasSize(64).isNotEqualTo(session.token());
    }
}
