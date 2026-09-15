# Initial API contract

Base path: `/api/v1`

## Heroes

### `GET /heroes`

Returns the current hero catalog. When the database contains ingested heroes, the API returns that PostgreSQL-backed catalog; the small seed catalog is only a fallback for an empty database.

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

Example response shape:

```json
[
  {
    "heroId": 65,
    "heroName": "Claude",
    "score": 52.0,
    "breakdown": {
      "laneFit": 30.0,
      "roleFit": 15.0,
      "metaScore": 7.0,
      "counterScore": 0.0,
      "synergyScore": 0.0
    },
    "reasons": [
      "Fits GOLD lane",
      "Matches preferred MARKSMAN role"
    ]
  }
]
```

The score breakdown is part of the contract. The UI should be able to explain a recommendation rather than display only a rank.

## MLBB account linking and Chunky session

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

Verifies account ownership, upserts the normalized MLBB profile into `app_user`, creates a local Chunky session, and returns the persisted user profile. The response also sets an HTTP-only `chunky_session` cookie. Raw session tokens are never stored in PostgreSQL; only their SHA-256 hashes are persisted.

Example response shape:

```json
{
  "userId": "9b97730b-c85f-45d2-b31f-07c393c8d776",
  "roleId": 123456789,
  "zoneId": 1234,
  "nickname": "MLBB Player",
  "avatarUrl": "https://...",
  "level": 100,
  "rankLevel": 200,
  "highestRankLevel": 250,
  "registeredCountry": "BD"
}
```

### `GET /auth/mlbb/me`

Returns the locally persisted authenticated user for the active `chunky_session` cookie. Returns `401` when there is no valid session.

### `POST /auth/mlbb/logout`

Revokes the active local session and expires the browser cookie. The verified `app_user` record remains stored.

For local development, the session cookie is HTTP-only, `SameSite=Lax`, and not marked `Secure`. Set `CHUNKY_SECURE_COOKIE=true` behind HTTPS in production. Session lifetime defaults to 30 days and can be changed through `CHUNKY_SESSION_DAYS`.

## Planned next APIs

- `/meta/heroes` — current and historical pick/ban/win rate
- `/leaderboards/heroes` — popularity among verified Chunky users
- `/community/posts` — community feed
- `/community/posts/{id}/comments`
- `/me/heroes` — imported/favorited hero pool
