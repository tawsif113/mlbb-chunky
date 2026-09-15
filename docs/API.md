# API contract

Base path: `/api/v1`

## Heroes

### `GET /heroes`

Returns the current PostgreSQL-backed hero catalog, including image URL, roles, lanes and the latest configured meta snapshot when available.

### `GET /heroes/{heroId}`

Returns one hero.

## Draft Assistant

### `POST /draft/recommendations`

Example request:

```json
{
  "lane": "GOLD",
  "preferredRole": "MARKSMAN",
  "alliedHeroIds": [6],
  "enemyHeroIds": [20],
  "bannedHeroIds": [84]
}
```

The response contains ranked recommendations with a transparent score breakdown for lane fit, role fit, meta, counters and synergy.

The meta component uses the latest snapshot for the configured `app.mlbb.meta.rank-scope` and `app.mlbb.meta.period-days` values.

## MLBB account linking

### `POST /auth/mlbb/verification-code`

```json
{
  "roleId": 123456789,
  "zoneId": 1234
}
```

Requests an in-game verification code through the configured identity-provider adapter.

### `POST /auth/mlbb/verify`

```json
{
  "roleId": 123456789,
  "zoneId": 1234,
  "verificationCode": "1234"
}
```

Verifies account ownership, upserts the normalized profile into `app_user`, creates a Chunky session and returns the local authenticated user. The raw session token is sent only in an HTTP-only cookie; only its SHA-256 hash is stored.

### `GET /auth/mlbb/me`

Returns the currently authenticated Chunky user from the session cookie.

### `POST /auth/mlbb/logout`

Revokes the current Chunky session and clears the cookie.

## Meta

### `GET /meta/heroes`

Query parameters:

- `rankScope`: `all`, `epic`, `legend`, `mythic`, `honor`, `glory` (default `all`)
- `periodDays`: `1`, `3`, `7`, `15`, `30` (default `7`)

Example:

```text
GET /api/v1/meta/heroes?rankScope=all&periodDays=7
```

Returns the most recent stored snapshot for every hero in that rank/time window, including pick, ban and win rates and the snapshot timestamp.

## Planned next APIs

- `/leaderboards/heroes` — popularity among verified Chunky users
- `/community/posts` — community feed
- `/community/posts/{id}/comments`
- `/me/heroes` — imported/favorited hero pool
