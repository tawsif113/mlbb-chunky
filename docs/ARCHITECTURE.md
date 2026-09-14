# Architecture

## Goal

MLBB Chunky is a draft-decision and community product, not a mirror of the Mobile Legends website.

The core experience is:

1. identify what the current draft needs;
2. rank viable heroes;
3. explain *why* each recommendation is good now;
4. let verified MLBB players build reputation, preferences and community around those decisions.

## Monorepo

```text
backend/   Spring Boot API and scheduled MLBB data ingestion
frontend/  Next.js web application
docs/      product and technical decisions
infra/     local/deployment infrastructure
```

## Backend domains

### `auth`

Links an MLBB account to an MLBB Chunky user.

Planned flow:

```text
roleId + zoneId
      |
      v
send in-game verification code
      |
      v
roleId + zoneId + code
      |
      v
MLBB identity provider verifies ownership
      |
      v
fetch permitted MLBB profile
      |
      v
create/update MLBB Chunky user
      |
      v
issue MLBB Chunky session
```

The provider is an adapter. The application must not depend directly on Rone Arena, RapidAPI, private Moonton endpoints, or an upstream JWT format.

### `hero`

Canonical hero metadata and current/historical meta statistics.

### `draft`

Ranks heroes for a concrete draft. Initial score components:

- lane fit
- role fit
- ally synergy
- enemy counter score
- current win rate
- pick/ban pressure
- user familiarity/mastery (after account stats are available)
- patch freshness/confidence

Every recommendation should expose its score breakdown. Avoid a black-box recommendation number.

### `leaderboard`

Community popularity among **verified registered users**, kept separate from global MLBB pick rate.

Examples:

- most favorited hero
- most played hero among Chunky users
- trending this week
- popularity by rank
- popularity by region/country
- popularity by lane

### `community`

Hero discussions, posts, comments and reactions. Later: LFG/team recruitment, draft sharing and guides.

## Data boundaries

Keep these concepts separate:

- `MlbbAccount`: external identity (`roleId`, `zoneId`) and imported profile facts.
- `User`: local MLBB Chunky identity, permissions and community reputation.
- `HeroMetaSnapshot`: game-wide pick/ban/win-rate observation at a point in time.
- `UserHeroSnapshot`: imported stats belonging to one verified account.
- `HeroPopularity`: aggregate behavior of Chunky's own registered users.

## External MLBB integrations

Use ports/adapters:

```text
MlbbIdentityProvider
MlbbHeroDataProvider
MlbbMetaDataProvider
MlbbPlayerStatsProvider
```

Providers must be replaceable because the known rich account endpoints are not a stable documented Moonton public developer API.

Never store MLBB passwords. Never expose upstream access/JWT tokens to the browser.
