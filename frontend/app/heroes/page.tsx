"use client";

import { useEffect, useMemo, useState } from "react";
import { SiteNav } from "../components/SiteNav";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type Hero = {
  id: number;
  name: string;
  roles: string[];
  lanes: string[];
  winRate: number;
  pickRate: number;
  banRate: number;
  strongAgainst: number[];
  synergizesWith: number[];
};

export default function HeroesPage() {
  const [heroes, setHeroes] = useState<Hero[]>([]);
  const [query, setQuery] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    fetch(`${API_BASE}/api/v1/heroes`)
      .then((response) => {
        if (!response.ok) throw new Error(`Backend returned ${response.status}`);
        return response.json();
      })
      .then(setHeroes)
      .catch((cause) => setError(cause instanceof Error ? cause.message : "Could not load heroes"));
  }, []);

  const filtered = useMemo(
    () => heroes.filter((hero) => hero.name.toLowerCase().includes(query.toLowerCase())),
    [heroes, query],
  );

  return (
    <main>
      <SiteNav />
      <section className="page-header compact">
        <p className="eyebrow">HERO EXPLORER</p>
        <h1>Know the pool.</h1>
        <p className="lede">The current screen uses the backend seed catalog. Live MLBB ingestion will expand this into the complete roster.</p>
      </section>

      <div className="toolbar">
        <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search heroes…" />
        <span>{filtered.length} heroes</span>
      </div>

      {error && <div className="panel error-text">{error}. Make sure the backend is running on {API_BASE}.</div>}

      <section className="hero-grid">
        {filtered.map((hero) => (
          <article className="hero-card" key={hero.id}>
            <div className="hero-id">#{hero.id}</div>
            <h2>{hero.name}</h2>
            <div className="chips">
              {hero.roles.map((role) => <span key={role}>{role}</span>)}
              {hero.lanes.map((lane) => <span key={lane}>{lane}</span>)}
            </div>
            <div className="stat-row">
              <div><strong>{(hero.winRate * 100).toFixed(1)}%</strong><span>Win</span></div>
              <div><strong>{(hero.pickRate * 100).toFixed(1)}%</strong><span>Pick</span></div>
              <div><strong>{(hero.banRate * 100).toFixed(1)}%</strong><span>Ban</span></div>
            </div>
          </article>
        ))}
      </section>
    </main>
  );
}
