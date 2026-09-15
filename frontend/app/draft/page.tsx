"use client";

import { FormEvent, useState } from "react";
import { SiteNav } from "../components/SiteNav";

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

const parseIds = (value: string) =>
  value
    .split(",")
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isFinite(item) && item > 0);

export default function DraftPage() {
  const [lane, setLane] = useState("GOLD");
  const [role, setRole] = useState("MARKSMAN");
  const [allies, setAllies] = useState("");
  const [enemies, setEnemies] = useState("");
  const [bans, setBans] = useState("");
  const [results, setResults] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

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
          alliedHeroIds: parseIds(allies),
          enemyHeroIds: parseIds(enemies),
          bannedHeroIds: parseIds(bans),
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
        <p className="lede">Use hero IDs for the seed catalog for now. The next data-ingestion step will replace IDs with searchable hero selectors.</p>
      </section>

      <section className="workspace two-column">
        <form className="panel form-panel" onSubmit={submit}>
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
          <label>
            Allied hero IDs
            <input value={allies} onChange={(e) => setAllies(e.target.value)} placeholder="e.g. 6, 20" />
          </label>
          <label>
            Enemy hero IDs
            <input value={enemies} onChange={(e) => setEnemies(e.target.value)} placeholder="e.g. 84" />
          </label>
          <label>
            Banned hero IDs
            <input value={bans} onChange={(e) => setBans(e.target.value)} placeholder="e.g. 65" />
          </label>
          <button type="submit" disabled={loading}>{loading ? "Scoring draft…" : "Recommend heroes"}</button>
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
          {results.map((item, index) => (
            <article className="result-card" key={item.heroId}>
              <div className="result-rank">#{index + 1}</div>
              <div>
                <h2>{item.heroName}</h2>
                <p className="score">{item.score.toFixed(1)} score</p>
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
          ))}
        </div>
      </section>
    </main>
  );
}
