# Live MLBB data ingestion

The backend can ingest the public hero roster and current hero meta snapshots from the unofficial Rone Arena API and store them in PostgreSQL.

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

The provider first requests Rone's bulk `/api/heroes/rank` endpoint. If that upstream-backed route is temporarily unavailable, 7/15/30-day imports fall back to `/api/academy/heroes/{heroId}/trends` for each hero and average the daily pick, ban and win rates across the requested window. The 1-day and 3-day imports currently require the bulk endpoint because the Academy trend endpoint does not expose those windows.

Each successful run appends a historical snapshot to `hero_meta_snapshot`; it does not overwrite earlier snapshots.

The configured `MLBB_META_RANK_SCOPE` and `MLBB_META_PERIOD_DAYS` also determine which latest snapshot the hero catalog and Draft Assistant use. Defaults are `all` and `7`.

Read a stored snapshot through:

```text
GET /api/v1/meta/heroes?rankScope=all&periodDays=7
```

The frontend `/meta` page lets you inspect stored snapshots and sort them by win, pick or ban rate.

Startup meta ingestion is best-effort: a provider outage is logged but does not terminate the Chunky application. When a previous snapshot exists, the application continues serving that stored data.

## Production note

Do not leave startup sync enabled permanently in production. Startup flags are intended for development and one-time imports. A scheduled/background ingestion job should replace them once provider reliability, refresh cadence and retention policy are finalized.
