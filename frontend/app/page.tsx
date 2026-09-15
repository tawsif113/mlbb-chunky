import Link from "next/link";
import { SiteNav } from "./components/SiteNav";

const features = [
  {
    title: "Draft Assistant",
    text: "Set your lane, allies, enemies and bans. Chunky ranks available heroes and explains every score.",
    href: "/draft",
    kicker: "Pick smarter",
  },
  {
    title: "Community Meta",
    text: "Explore hero data now and grow into verified-player popularity segmented by rank, lane and region.",
    href: "/meta",
    kicker: "Read the meta",
  },
  {
    title: "Hero Rooms",
    text: "A home for matchup advice, builds, patches and draft discussions around the heroes you play.",
    href: "/community",
    kicker: "Talk strategy",
  },
  {
    title: "MLBB-linked identity",
    text: "Link your own MLBB account with User ID, Zone ID and an in-game verification code — no MLBB password stored.",
    href: "/link-account",
    kicker: "Verify ownership",
  },
];

export default function Home() {
  return (
    <main>
      <SiteNav />

      <section className="hero">
        <p className="eyebrow">DRAFT WITH CONTEXT</p>
        <h1>Know what to pick.<br />Know why it works.</h1>
        <p className="lede">
          A Mobile Legends companion built around draft decisions, transparent recommendations,
          verified-player popularity and community knowledge.
        </p>
        <div className="actions">
          <Link className="button" href="/draft">Open Draft Assistant</Link>
          <Link className="button secondary" href="/heroes">Explore Heroes</Link>
        </div>
      </section>

      <section className="grid">
        {features.map((feature) => (
          <Link className="feature-card" href={feature.href} key={feature.title}>
            <span className="card-kicker">{feature.kicker}</span>
            <h2>{feature.title}</h2>
            <p>{feature.text}</p>
            <span className="card-link">Open →</span>
          </Link>
        ))}
      </section>

      <section className="split-callout">
        <div>
          <p className="eyebrow">VERIFIED COMMUNITY</p>
          <h2>Popularity should mean more than global pick rate.</h2>
          <p>Chunky will separately track the official meta and what verified Chunky players actually favorite and play.</p>
        </div>
        <Link className="button secondary" href="/leaderboard">Open leaderboard</Link>
      </section>

      <footer>
        Mobile Legends: Bang Bang and related marks belong to their respective owners. MLBB Chunky is an unofficial community project. Optional account-data integration is powered by{" "}
        <a href="https://arena.rone.dev" target="_blank" rel="noreferrer">Rone Arena</a> when enabled.
      </footer>
    </main>
  );
}
