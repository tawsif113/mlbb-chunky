"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { SiteNav } from "../components/SiteNav";
import { Hero, HeroPicker } from "./HeroPicker";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const lanes = ["GOLD", "EXP", "MID", "JUNGLE", "ROAM"];
const roles = ["MARKSMAN", "FIGHTER", "MAGE", "ASSASSIN", "TANK", "SUPPORT"];

type Recommendation = {
  heroId: number;
  heroName: string;
  score: number;
  breakdown: Record<string, number>;
  reasons: string[];
};

export default function DraftPage() {
  const [heroes, setHeroes] = useState<Hero[]>([]);
  const [heroLoading, setHeroLoading] = useState(true);
  const [heroError, setHeroError] = useState("");
  const [lane, setLane] = useState("GOLD");
  const [role, setRole] = useState("MARKSMAN");
  const [allies, setAllies] = useState<number[]>([]);
  const [enemies, setEnemies] = useState<number[]>([]);
  const [bans, setBans] = useState<number[]>([]);
  const [results, setResults] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    fetch(`${API_BASE}/api/v1/heroes`)
      .then((response) => {
        if (!response.ok) throw new Error(`Backend returned ${response.status}`);
        return response.json();
      })
      .then((catalog: Hero[]) => {
        if (!active) return;
        setHeroes(catalog);
        setHeroError("");
      })
      .catch((cause) => {
        if (!active) return;
        setHeroError(cause instanceof Error ? cause.message : "Could not load hero catalog");
      })
      .finally(() => {
        if (active) setHeroLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const heroById = useMemo(() => new Map(heroes.map((hero) => [hero.id, hero])), [heroes]);

  const blockedForAllies = useMemo(() => new Set([...enemies, ...bans]), [bans, enemies]);
  const blockedForEnemies = useMemo(() => new Set([...allies, ...bans]), [allies, bans]);
  const blockedForBans = useMemo(() => new Set([...allies, ...enemies]), [allies, enemies]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const response = await fetch(`${API_BASE}/api/v1/draft/recommendations`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          lane,
          preferredRole: role,
          alliedHeroIds: allies,
          enemyHeroIds: enemies,
          bannedHeroIds: bans,
        }),
      });

      if (!response.ok) throw new Error(`Backend returned ${response.status}`);
      setResults(await response.json());
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not load recommendations");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main>
      <SiteNav />
      <section className="page-header">
        <p className="eyebrow">DRAFT ASSISTANT</p>
        <h1>Build the draft.<br />Get an explained pick.</h1>
        <p className="lede">Search the live hero catalog, add allies, enemies and bans, then let Chunky rank available picks with a transparent score breakdown.</p>
      </section>

      {heroError && (
        <div className="panel error-text draft-catalog-error">
          Could not load the hero catalog: {heroError}. Make sure the backend is running on {API_BASE}.
        </div>
      )}

      <section className="workspace two-column draft-workspace">
        <form className="panel form-panel" onSubmit={submit}>
          <div className="draft-select-row">
            <label>
              Lane
              <select value={lane} onChange={(e) => setLane(e.target.value)}>
                {lanes.map((item) => <option key={item}>{item}</option>)}
              </select>
            </label>
            <label>
              Preferred role
              <select value={role} onChange={(e) => setRole(e.target.value)}>
                {roles.map((item) => <option key={item}>{item}</option>)}
              </select>
            </label>
          </div>

          <HeroPicker
            label="Allied heroes"
            heroes={heroes}
            selectedIds={allies}
            blockedIds={blockedForAllies}
            onChange={setAllies}
            placeholder="Search ally by name, role or lane…"
            loading={heroLoading}
          />

          <HeroPicker
            label="Enemy heroes"
            heroes={heroes}
            selectedIds={enemies}
            blockedIds={blockedForEnemies}
            onChange={setEnemies}
            placeholder="Search enemy by name, role or lane…"
            loading={heroLoading}
          />

          <HeroPicker
            label="Banned heroes"
            heroes={heroes}
            selectedIds={bans}
            blockedIds={blockedForBans}
            onChange={setBans}
            placeholder="Search banned hero…"
            loading={heroLoading}
          />

          <div className="draft-summary">
            <span>{heroes.length || "—"} heroes loaded</span>
            <span>{allies.length} allies</span>
            <span>{enemies.length} enemies</span>
            <span>{bans.length} bans</span>
          </div>

          <button type="submit" disabled={loading || heroLoading || Boolean(heroError)}>
            {loading ? "Scoring draft…" : "Recommend heroes"}
          </button>
          {error && <p className="error-text">{error}. Make sure the backend is running on {API_BASE}.</p>}
        </form>

        <div className="result-stack">
          {results.length === 0 && !loading && (
            <div className="panel empty-state">
              <span className="card-kicker">No recommendation yet</span>
              <h2>Your ranked picks will appear here.</h2>
              <p>Chunky exposes lane fit, role fit, meta score, counters and synergy instead of hiding the reasoning.</p>
            </div>
          )}
          {results.map((item, index) => {
            const hero = heroById.get(item.heroId);
            return (
              <article className="result-card" key={item.heroId}>
                <div className="result-rank">#{index + 1}</div>
                <div className="result-hero-copy">
                  <div className="result-hero-heading">
                    {hero?.imageUrl ? <img src={hero.imageUrl} alt={`${item.heroName} portrait`} /> : null}
                    <div>
                      <h2>{item.heroName}</h2>
                      <p className="score">{item.score.toFixed(1)} score</p>
                    </div>
                  </div>
                  {hero && (
                    <div className="chips result-hero-tags">
                      {hero.roles.map((heroRole) => <span key={heroRole}>{heroRole}</span>)}
                      {hero.lanes.map((heroLane) => <span key={heroLane}>{heroLane}</span>)}
                    </div>
                  )}
                  <div className="chips">
                    {Object.entries(item.breakdown).map(([key, value]) => (
                      <span key={key}>{key}: {value}</span>
                    ))}
                  </div>
                  <ul>
                    {item.reasons.map((reason) => <li key={reason}>{reason}</li>)}
                  </ul>
                </div>
              </article>
            );
          })}
        </div>
      </section>
    </main>
  );
}
