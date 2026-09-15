# Live MLBB data ingestion

The backend can ingest the public hero roster, current hero meta snapshots, and draft relationships from the unofficial Rone Arena API and store them in PostgreSQL.

## Hero catalog sync

Keep the provider disabled by default. To perform a one-time hero catalog sync on application startup:

```bash
export MLBB_DATA_PROVIDER=rone
export MLBB_HERO_SYNC_ON_STARTUP=true
./gradlew bootRun
```

The hero provider reads `https://arena.rone.dev/api/academy/heroes`, then uses role and lane filters to enrich each hero before persisting it.

After a successful sync, `GET /api/v1/heroes` and the Draft Assistant read the database catalog. If the database has no heroes yet, the application falls back to the small seed catalog.

## Meta snapshot sync

Rone exposes hero rank statistics with pick, ban and win rates for 1, 3, 7, 15 and 30 day windows and the rank scopes `all`, `epic`, `legend`, `mythic`, `honor` and `glory`.

For a one-time 7-day all-ranks sync:

```bash
export MLBB_DATA_PROVIDER=rone
export MLBB_META_SYNC_ON_STARTUP=true
export MLBB_META_RANK_SCOPE=all
export MLBB_META_PERIOD_DAYS=7
./gradlew bootRun
```

The provider first requests Rone's bulk `/api/heroes/rank` endpoint. If that route is temporarily unavailable, 7/15/30-day imports fall back to `/api/heroes/{heroId}/trends` for each hero and then to the Academy trend endpoint if needed. The per-hero fallback averages the returned daily pick, ban and win rates across the requested window. The 1-day and 3-day imports currently require the bulk endpoint because the trend endpoints do not expose those windows.

Each successful run appends a historical snapshot to `hero_meta_snapshot`; it does not overwrite earlier snapshots.

The configured `MLBB_META_RANK_SCOPE` and `MLBB_META_PERIOD_DAYS` also determine which latest snapshot the hero catalog and Draft Assistant use. Defaults are `all` and `7`.

Read a stored snapshot through:

```text
GET /api/v1/meta/heroes?rankScope=all&periodDays=7
```

The frontend `/meta` page lets you inspect stored snapshots and sort them by win, pick or ban rate.

Startup meta ingestion is best-effort: a provider outage is logged but does not terminate the Chunky application. When a previous snapshot exists, the application continues serving that stored data.

## Counter and synergy sync

The Draft Assistant can ingest matchup and teammate relationships for the same configured rank scope and period as the meta snapshot.

For a one-time relationship import:

```bash
export MLBB_DATA_PROVIDER=rone
export MLBB_RELATION_SYNC_ON_STARTUP=true
export MLBB_META_RANK_SCOPE=all
export MLBB_META_PERIOD_DAYS=7
./gradlew bootRun
```

For every catalog hero, the provider requests:

- `/api/heroes/{heroId}/counters` for heroes that counter the selected hero.
- `/api/heroes/{heroId}/compatibility` for heroes that pair well with the selected hero.

If either primary endpoint fails for one hero, Chunky falls back to the Academy `/counters` or `/teammates` endpoint for that hero. Individual hero failures are skipped rather than aborting the whole import.

Relationships are stored in `hero_relationship` with their rank scope, period and Rone win-rate impact value. A successful response replaces the previous rows for that hero/type/scope/window, while failed hero requests leave the last stored rows untouched.

The database-backed hero catalog translates those raw relationships into the existing domain model:

- A counter record for target hero A with related hero B makes B's `strongAgainst` set contain A.
- Compatibility records populate `synergizesWith` in both directions for draft scoring.

You can verify imported relationships with the existing hero API:

```text
GET /api/v1/heroes/{heroId}
```

The response should contain non-empty `strongAgainst` and/or `synergizesWith` arrays for heroes where Rone publishes relationship data.

Startup relationship ingestion is also best-effort. Provider failures are logged and Chunky remains online using any previously stored relationships.

## Production note

Do not leave startup sync enabled permanently in production. Startup flags are intended for development and one-time imports. A scheduled/background ingestion job should replace them once provider reliability, refresh cadence and retention policy are finalized.
