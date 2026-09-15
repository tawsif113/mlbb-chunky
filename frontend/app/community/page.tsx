import Link from "next/link";
import { SiteNav } from "../components/SiteNav";

const rooms = [
  { title: "Draft Lab", text: "Share drafts, counter-pick questions and composition ideas.", href: "/draft" },
  { title: "Hero Matchups", text: "Discuss difficult lanes, counterplay and role-specific decisions.", href: "/heroes" },
  { title: "Meta Watch", text: "Talk about patches, rising picks, bans and rank-specific trends.", href: "/meta" },
];

export default function CommunityPage() {
  return (
    <main>
      <SiteNav />
      <section className="page-header compact">
        <p className="eyebrow">COMMUNITY</p>
        <h1>Strategy should be discussable.</h1>
        <p className="lede">Community posting comes after Chunky sessions are in place. For now, these rooms route players into the parts of the product they will eventually discuss around.</p>
      </section>

      <section className="grid">
        {rooms.map((room) => (
          <Link className="feature-card" href={room.href} key={room.title}>
            <span className="card-kicker">Community room</span>
            <h2>{room.title}</h2>
            <p>{room.text}</p>
            <span className="card-link">Enter →</span>
          </Link>
        ))}
        <Link className="feature-card" href="/link-account">
          <span className="card-kicker">Identity</span>
          <h2>Verified players</h2>
          <p>Linking MLBB ownership will become the trust layer for posting, reactions, reputation and leaderboard participation.</p>
          <span className="card-link">Link account →</span>
        </Link>
      </section>
    </main>
  );
}
