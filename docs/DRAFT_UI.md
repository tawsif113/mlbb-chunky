# Draft Assistant UI

The Draft Assistant loads the persisted hero catalog from `GET /api/v1/heroes` and uses visual searchable selectors for allies, enemies and bans.

- Search matches hero name, role and lane.
- Hero portraits come from the ingested catalog.
- A hero cannot be selected in more than one draft group.
- The frontend continues sending numeric hero IDs to `POST /api/v1/draft/recommendations`.
- Recommendation cards reuse the catalog to show portrait, role and lane metadata next to the existing score breakdown.
