# MLBB Chunky

MLBB Chunky is a companion and community platform for Mobile Legends: Bang Bang players.

The product is centered on one question: **which hero should I pick right now, and why?**

## Product pillars

- **Draft Assistant** — recommend heroes from lane/role requirements, allied picks, enemy picks, counters, synergies, and current meta statistics.
- **Hero & Meta Explorer** — searchable hero data, skills, roles, lanes, pick/ban/win rates, counters, synergies, and patch-aware trends.
- **MLBB-native identity** — account linking through MLBB role/user ID + zone/server ID + in-game verification code, rather than a conventional password-first login.
- **Community Popularity Leaderboard** — aggregate which heroes registered users actually play/favorite, with global and segmented rankings.
- **Community** — posts, discussions, comments, reactions, hero-specific threads, and eventually LFG/team-finding.

## Repository layout

```text
mlbb-chunky/
├── backend/       # Java + Spring Boot API
├── frontend/      # Next.js + TypeScript web client
├── docs/          # architecture, product and integration notes
├── infra/         # local infrastructure
└── .github/       # CI workflows
```

## Authentication direction

MLBB Chunky does **not** intend to store an MLBB password. The planned account-linking flow is:

1. Player enters MLBB Role/User ID and Zone/Server ID.
2. Backend asks the configured MLBB identity provider to send an in-game verification code.
3. Player enters that verification code.
4. Backend verifies account ownership and fetches the player's permitted MLBB profile data.
5. MLBB Chunky creates its own application session for the verified player.

The upstream MLBB integration is deliberately isolated behind a provider interface because the available endpoints are not a stable, documented public Moonton developer API.

## Status

Initial architecture/scaffolding is under development.
