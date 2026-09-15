package com.mlbbchunky.auth.infrastructure;

import com.mlbbchunky.auth.application.AuthRepository;
import com.mlbbchunky.auth.application.AuthenticatedUser;
import com.mlbbchunky.auth.application.MlbbIdentityProvider.VerifiedMlbbProfile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcAuthRepository implements AuthRepository {
    private final JdbcClient jdbc;

    public JdbcAuthRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public AuthenticatedUser upsertVerifiedUser(VerifiedMlbbProfile profile) {
        UUID candidateId = UUID.randomUUID();
        String nickname = profile.nickname() == null || profile.nickname().isBlank()
                ? "MLBB Player"
                : profile.nickname();

        jdbc.sql("""
                insert into app_user (
                    id,
                    mlbb_role_id,
                    mlbb_zone_id,
                    nickname,
                    avatar_url,
                    registered_country,
                    level,
                    rank_level,
                    highest_rank_level,
                    created_at,
                    updated_at
                ) values (
                    :id,
                    :roleId,
                    :zoneId,
                    :nickname,
                    :avatarUrl,
                    :registeredCountry,
                    :level,
                    :rankLevel,
                    :highestRankLevel,
                    now(),
                    now()
                )
                on conflict (mlbb_role_id, mlbb_zone_id) do update
                set nickname = excluded.nickname,
                    avatar_url = excluded.avatar_url,
                    registered_country = excluded.registered_country,
                    level = excluded.level,
                    rank_level = excluded.rank_level,
                    highest_rank_level = excluded.highest_rank_level,
                    updated_at = now()
                """)
                .param("id", candidateId)
                .param("roleId", profile.roleId())
                .param("zoneId", profile.zoneId())
                .param("nickname", nickname)
                .param("avatarUrl", profile.avatarUrl())
                .param("registeredCountry", profile.registeredCountry())
                .param("level", profile.level())
                .param("rankLevel", profile.rankLevel())
                .param("highestRankLevel", profile.highestRankLevel())
                .update();

        return jdbc.sql("""
                select id,
                       mlbb_role_id,
                       mlbb_zone_id,
                       nickname,
                       avatar_url,
                       level,
                       rank_level,
                       highest_rank_level,
                       registered_country
                from app_user
                where mlbb_role_id = :roleId
                  and mlbb_zone_id = :zoneId
                """)
                .param("roleId", profile.roleId())
                .param("zoneId", profile.zoneId())
                .query(this::mapUser)
                .single();
    }

    @Override
    public void createSession(UUID userId, String tokenHash, Instant expiresAt) {
        jdbc.sql("""
                insert into app_session (id, user_id, token_hash, expires_at)
                values (:id, :userId, :tokenHash, :expiresAt)
                """)
                .param("id", UUID.randomUUID())
                .param("userId", userId)
                .param("tokenHash", tokenHash)
                .param("expiresAt", OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
                .update();
    }

    @Override
    public Optional<AuthenticatedUser> findUserByActiveSession(String tokenHash, Instant now) {
        Optional<AuthenticatedUser> user = jdbc.sql("""
                select u.id,
                       u.mlbb_role_id,
                       u.mlbb_zone_id,
                       u.nickname,
                       u.avatar_url,
                       u.level,
                       u.rank_level,
                       u.highest_rank_level,
                       u.registered_country
                from app_session s
                join app_user u on u.id = s.user_id
                where s.token_hash = :tokenHash
                  and s.revoked_at is null
                  and s.expires_at > :now
                """)
                .param("tokenHash", tokenHash)
                .param("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .query(this::mapUser)
                .optional();

        if (user.isPresent()) {
            jdbc.sql("update app_session set last_seen_at = now() where token_hash = :tokenHash")
                    .param("tokenHash", tokenHash)
                    .update();
        }

        return user;
    }

    @Override
    public void revokeSession(String tokenHash) {
        jdbc.sql("""
                update app_session
                set revoked_at = coalesce(revoked_at, now())
                where token_hash = :tokenHash
                """)
                .param("tokenHash", tokenHash)
                .update();
    }

    private AuthenticatedUser mapUser(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new AuthenticatedUser(
                rs.getObject("id", UUID.class),
                rs.getLong("mlbb_role_id"),
                rs.getLong("mlbb_zone_id"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                (Integer) rs.getObject("level"),
                (Integer) rs.getObject("rank_level"),
                (Integer) rs.getObject("highest_rank_level"),
                rs.getString("registered_country")
        );
    }
}
