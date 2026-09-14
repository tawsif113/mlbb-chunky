# Initial API contract

Base path: `/api/v1`

## Heroes

### `GET /heroes`

Returns the current hero catalog.

### `GET /heroes/{heroId}`

Returns one hero.

The first branch uses a tiny seed catalog so the API and scoring engine can be developed before the live ingestion adapter is added.

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
  "verificationCode": "123456"
}
```

Verifies account ownership and returns normalized MLBB profile fields from the provider.

**Current state:** no external identity provider is wired yet. These endpoints deliberately return `503 Service Unavailable` until a provider adapter is configured.

## Planned next APIs

- `/meta/heroes` — current and historical pick/ban/win rate
- `/leaderboards/heroes` — popularity among verified Chunky users
- `/community/posts` — community feed
- `/community/posts/{id}/comments`
- `/me/heroes` — imported/favorited hero pool
