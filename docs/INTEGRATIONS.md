# External integrations

## Rone Arena — optional MLBB identity adapter

MLBB Chunky includes an **optional** adapter for the hosted Rone Arena API.

Rone Arena is community-maintained and explicitly unofficial. It is not Moonton authentication and must not be presented as an official Mobile Legends developer integration.

### Why we use it narrowly

The stable account flow currently provides exactly what we need for ownership linking:

1. `POST /api/user/auth/send-vc` — request a 4-digit in-game code.
2. `POST /api/user/auth/login` — verify `role_id + zone_id + vc`, returning an upstream JWT.
3. `GET /api/user/info` — fetch normalized account identity/profile data.
4. `POST /api/user/auth/logout` — invalidate the upstream session.

The adapter consumes the upstream JWT server-side, fetches the profile, attempts logout, and **never returns that JWT to the MLBB Chunky browser**.

Enable locally with:

```bash
export MLBB_IDENTITY_PROVIDER=rone
```

Default is `none`, which causes the account-link endpoints to fail closed with HTTP 503.

### Usage boundary

Rone Arena's hosted API terms state that player-facing endpoints require the player's own authentication and must not be used against accounts that are not yours. This matches Chunky's intended account-linking flow: the player must obtain the verification code from their own in-game mail.

Do not add arbitrary-player profile scraping through authenticated user endpoints.

### Stability

The richer endpoints for statistics, matches, frequent heroes and friends are currently marked deprecated by Rone Arena. They may be useful for experiments but must **not** become hard dependencies for core product features until a supported replacement exists.

### Attribution / commercial use

The hosted API asks products presenting its data to visibly credit Rone Arena. Its terms also say commercial use of the hosted endpoint is by arrangement. Before a commercial launch, contact RoneAI and confirm the intended load/use.

Sources:

- https://arena.rone.dev/api/docs
- https://github.com/ridwaanhall/rone-arena-api
- https://github.com/ridwaanhall/rone-arena-api/blob/main/HOSTED_API_TERMS.md
