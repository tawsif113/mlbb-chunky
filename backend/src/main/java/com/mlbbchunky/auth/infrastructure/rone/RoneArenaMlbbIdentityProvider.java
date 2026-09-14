package com.mlbbchunky.auth.infrastructure.rone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mlbbchunky.auth.application.MlbbIdentityProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
@ConditionalOnProperty(prefix = "app.mlbb", name = "identity-provider", havingValue = "rone")
public class RoneArenaMlbbIdentityProvider implements MlbbIdentityProvider {
    private final RestClient client;

    public RoneArenaMlbbIdentityProvider(
            RestClient.Builder builder,
            @Value("${app.mlbb.rone.base-url}") String baseUrl
    ) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void sendVerificationCode(long roleId, long zoneId) {
        SimpleEnvelope response = client.post()
                .uri("/user/auth/send-vc")
                .body(new VerificationRequest(roleId, zoneId))
                .retrieve()
                .body(SimpleEnvelope.class);

        if (response == null || response.code() != 0) {
            throw providerFailure(response == null ? null : response.msg());
        }
    }

    @Override
    public VerifiedMlbbProfile verify(long roleId, long zoneId, String verificationCode) {
        LoginEnvelope login = client.post()
                .uri("/user/auth/login")
                .body(new LoginRequest(roleId, zoneId, Integer.parseInt(verificationCode)))
                .retrieve()
                .body(LoginEnvelope.class);

        if (login == null || login.code() != 0 || login.data() == null || blank(login.data().jwt())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    login == null || blank(login.msg()) ? "MLBB verification failed" : login.msg()
            );
        }

        String upstreamJwt = login.data().jwt();
        try {
            UserInfoEnvelope profile = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/user/info").queryParam("lang", "en").build())
                    .headers(headers -> headers.setBearerAuth(upstreamJwt))
                    .retrieve()
                    .body(UserInfoEnvelope.class);

            if (profile == null || profile.code() != 0 || profile.data() == null) {
                throw providerFailure(profile == null ? null : profile.message());
            }

            UserInfoData data = profile.data();
            return new VerifiedMlbbProfile(
                    data.roleId() == null ? roleId : data.roleId(),
                    data.zoneId() == null ? zoneId : data.zoneId(),
                    blank(data.name()) ? "MLBB Player" : data.name(),
                    data.avatar(),
                    data.level(),
                    data.rankLevel(),
                    data.historyRankLevel(),
                    data.registeredCountry()
            );
        } finally {
            logoutQuietly(upstreamJwt);
        }
    }

    private void logoutQuietly(String upstreamJwt) {
        try {
            client.post()
                    .uri("/user/auth/logout")
                    .headers(headers -> headers.setBearerAuth(upstreamJwt))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // The upstream JWT is never returned to our browser or persisted by this adapter.
            // A failed best-effort logout should not erase a successful ownership verification.
        }
    }

    private ResponseStatusException providerFailure(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                blank(message) ? "MLBB identity provider request failed" : message
        );
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    record VerificationRequest(
            @JsonProperty("role_id") long roleId,
            @JsonProperty("zone_id") long zoneId
    ) {}

    record LoginRequest(
            @JsonProperty("role_id") long roleId,
            @JsonProperty("zone_id") long zoneId,
            @JsonProperty("vc") int verificationCode
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SimpleEnvelope(int code, String msg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LoginEnvelope(int code, String msg, LoginData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LoginData(String jwt) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UserInfoEnvelope(int code, String msg, String message, UserInfoData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UserInfoData(
            String avatar,
            String name,
            Integer level,
            @JsonProperty("rank_level") Integer rankLevel,
            @JsonProperty("history_rank_level") Integer historyRankLevel,
            @JsonProperty("reg_country") String registeredCountry,
            Long roleId,
            Long zoneId
    ) {}
}
