import Link from "next/link";
import { SiteNav } from "../components/SiteNav";

const preview = [
  ["1", "Most favorited hero", "Verified users"],
  ["2", "Most played GOLD laner", "Verified users"],
  ["3", "Fastest-rising hero", "7-day movement"],
];

export default function LeaderboardPage() {
  return (
    <main>
      <SiteNav />
      <section className="page-header compact">
        <p className="eyebrow">CHUNKY LEADERBOARD</p>
        <h1>Popularity from real linked players.</h1>
        <p className="lede">This leaderboard is intentionally separate from MLBB-wide pick rate. It will rank heroes based on activity from verified Chunky accounts.</p>
      </section>

      <section className="panel leaderboard-panel">
        {preview.map(([rank, title, note]) => (
          <div className="leader-row" key={rank}>
            <span className="leader-rank">#{rank}</span>
            <div><strong>{title}</strong><span>{note}</span></div>
            <span className="coming-pill">Awaiting verified-user data</span>
          </div>
        ))}
      </section>

      <div className="actions">
        <Link className="button" href="/link-account">Link your MLBB account</Link>
        <Link className="button secondary" href="/meta">Compare with meta</Link>
      </div>
    </main>
  );
}
