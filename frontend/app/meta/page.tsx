"use client";

import { useEffect, useMemo, useState } from "react";
import { SiteNav } from "../components/SiteNav";
import styles from "./page.module.css";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const periods = [1, 3, 7, 15, 30];
const ranks = ["all", "epic", "legend", "mythic", "honor", "glory"];

type MetaHero = {
  heroId: number;
  heroName: string;
  imageUrl?: string;
  rankScope: string;
  periodDays: number;
  pickRate: number;
  banRate: number;
  winRate: number;
  capturedAt: string;
};

type SortKey = "winRate" | "pickRate" | "banRate";

export default function MetaPage() {
  const [rankScope, setRankScope] = useState("all");
  const [periodDays, setPeriodDays] = useState(7);
  const [sortKey, setSortKey] = useState<SortKey>("winRate");
  const [heroes, setHeroes] = useState<MetaHero[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError("");

    fetch(`${API_BASE}/api/v1/meta/heroes?rankScope=${rankScope}&periodDays=${periodDays}`, {
      signal: controller.signal,
    })
      .then((response) => {
        if (!response.ok) throw new Error(`Backend returned ${response.status}`);
        return response.json() as Promise<MetaHero[]>;
      })
      .then(setHeroes)
      .catch((cause) => {
        if (cause instanceof DOMException && cause.name === "AbortError") return;
        setError(cause instanceof Error ? cause.message : "Could not load meta data");
      })
      .finally(() => setLoading(false));

    return () => controller.abort();
  }, [rankScope, periodDays]);

  const sorted = useMemo(
    () => [...heroes].sort((a, b) => b[sortKey] - a[sortKey]),
    [heroes, sortKey],
  );

  const capturedAt = sorted[0]?.capturedAt;

  return (
    <main>
      <SiteNav />
      <section className="page-header compact">
        <p className="eyebrow">META CENTER</p>
        <h1>Separate what is strong from what is popular.</h1>
        <p className="lede">Live MLBB win, pick and ban snapshots power this table and the Draft Assistant meta score.</p>
      </section>

      <section className={`panel ${styles.metaPanel}`}>
        <div className={styles.metaToolbar}>
          <label>
            Rank scope
            <select value={rankScope} onChange={(event) => setRankScope(event.target.value)}>
              {ranks.map((rank) => <option key={rank} value={rank}>{rank.toUpperCase()}</option>)}
            </select>
          </label>
          <label>
            Window
            <select value={periodDays} onChange={(event) => setPeriodDays(Number(event.target.value))}>
              {periods.map((days) => <option key={days} value={days}>{days} day{days === 1 ? "" : "s"}</option>)}
            </select>
          </label>
          <label>
            Sort by
            <select value={sortKey} onChange={(event) => setSortKey(event.target.value as SortKey)}>
              <option value="winRate">Win rate</option>
              <option value="pickRate">Pick rate</option>
              <option value="banRate">Ban rate</option>
            </select>
          </label>
          <div className={styles.metaStatus}>
            <strong>{heroes.length}</strong>
            <span>heroes</span>
            {capturedAt && <small>Snapshot {new Date(capturedAt).toLocaleString()}</small>}
          </div>
        </div>

        {error && <p className="error-text">{error}. Make sure a meta snapshot has been ingested.</p>}
        {!error && !loading && heroes.length === 0 && (
          <div className={`empty-state ${styles.metaEmpty}`}>
            <span className="card-kicker">No snapshot yet</span>
            <h2>Sync this rank and time window first.</h2>
            <p>Run the backend once with MLBB_META_SYNC_ON_STARTUP=true, then reload this page.</p>
          </div>
        )}

        {loading ? (
          <p className="status-text">Loading meta snapshot…</p>
        ) : heroes.length > 0 ? (
          <div className={styles.tableWrap}>
            <table className={styles.metaTable}>
              <thead>
                <tr><th>#</th><th>Hero</th><th>Win</th><th>Pick</th><th>Ban</th></tr>
              </thead>
              <tbody>
                {sorted.map((hero, index) => (
                  <tr key={hero.heroId}>
                    <td className={styles.metaRank}>{index + 1}</td>
                    <td>
                      <div className={styles.metaHero}>
                        {hero.imageUrl
                          ? <img src={hero.imageUrl} alt="" />
                          : <span className="hero-avatar-fallback">{hero.heroName.slice(0, 1)}</span>}
                        <div><strong>{hero.heroName}</strong><small>#{hero.heroId}</small></div>
                      </div>
                    </td>
                    <td><strong>{(hero.winRate * 100).toFixed(2)}%</strong></td>
                    <td>{(hero.pickRate * 100).toFixed(2)}%</td>
                    <td>{(hero.banRate * 100).toFixed(2)}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </main>
  );
}
