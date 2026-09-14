# Product roadmap

## Phase 1 — foundation

- monorepo structure
- hero catalog model
- draft recommendation API
- MLBB verification provider boundary
- basic web shell
- PostgreSQL/Flyway

## Phase 2 — live hero/meta data

- ingest complete hero catalog
- roles, lanes, specialties, skills and images
- counters and synergies
- pick/ban/win-rate snapshots
- patch-aware meta history
- hero explorer UI

## Phase 3 — Draft Assistant

Inputs:

- desired lane/role
- allied heroes already picked
- enemy heroes already picked
- optional banned heroes
- optional user hero pool

Outputs:

- top recommendations
- score breakdown
- counter reasons
- synergy reasons
- alternatives by difficulty/risk

Useful additions:

- blind-pick safety score
- first-pick priority
- team composition warnings (no frontline, low CC, physical/magic imbalance)
- last-pick counter recommendations

## Phase 4 — MLBB-native account linking

- send in-game verification code
- verify account ownership
- import nickname/avatar/rank/level where available
- local secure session
- periodic player-stat refresh with explicit consent

## Phase 5 — registered-user leaderboards

- favorite heroes
- most-played heroes
- trending heroes
- popularity by rank/country/lane
- opt-in public profiles

Important: this leaderboard represents Chunky users, not the global MLBB population.

## Phase 6 — community

- hero discussion boards
- posts/comments/reactions
- draft share links
- guides/build discussions
- follow users
- reputation/badges
- moderation/reporting

## Later ideas

- LFG/team finder
- squad pages
- patch-change impact analysis
- personalized hero-pool gaps
- 'what should I learn next?' recommendations
- matchup notes contributed by high-rank verified players
- esports draft explorer
