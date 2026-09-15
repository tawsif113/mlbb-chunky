# Live MLBB data ingestion

The backend can ingest the public hero roster from the unofficial Rone Arena API and store it in PostgreSQL.

## Local sync

Keep the provider disabled by default. To perform a one-time sync on application startup:

```bash
export MLBB_DATA_PROVIDER=rone
export MLBB_HERO_SYNC_ON_STARTUP=true
./gradlew bootRun
```

The provider reads `https://arena.rone.dev/api/academy/heroes`, then uses the endpoint's role and lane filters to enrich each hero before persisting it.

After a successful sync, `GET /api/v1/heroes` and the Draft Assistant read the database catalog. If the database has no heroes yet, the application falls back to the small seed catalog.

Do not enable startup sync permanently in production. A scheduled/background ingestion job should replace it once the ingestion pipeline and provider reliability policy are finalized.
