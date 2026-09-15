import Link from "next/link";
import { SiteNav } from "../components/SiteNav";

export default function MetaPage() {
  return (
    <main>
      <SiteNav />
      <section className="page-header compact">
        <p className="eyebrow">META CENTER</p>
        <h1>Separate what is strong from what is popular.</h1>
        <p className="lede">This area will combine official pick/ban/win-rate snapshots with Chunky-specific popularity from verified users.</p>
      </section>

      <section className="grid">
        <Link className="feature-card" href="/heroes">
          <span className="card-kicker">Available now</span>
          <h2>Hero Explorer</h2>
          <p>Browse the current backend catalog and baseline win, pick and ban values.</p>
          <span className="card-link">Browse heroes →</span>
        </Link>
        <Link className="feature-card" href="/draft">
          <span className="card-kicker">Use the data</span>
          <h2>Draft Assistant</h2>
          <p>Turn role, lane, matchup and synergy information into ranked draft recommendations.</p>
          <span className="card-link">Open draft →</span>
        </Link>
        <Link className="feature-card" href="/leaderboard">
          <span className="card-kicker">Chunky signal</span>
          <h2>Verified-player popularity</h2>
          <p>Track what linked Chunky players favorite and play, independently from the global meta.</p>
          <span className="card-link">Open leaderboard →</span>
        </Link>
        <div className="feature-card muted-card">
          <span className="card-kicker">Next ingestion milestone</span>
          <h2>Historical trends</h2>
          <p>1/3/7/15/30-day MLBB snapshots, rank segments and patch-aware movement will appear here after live ingestion lands.</p>
        </div>
      </section>
    </main>
  );
}
