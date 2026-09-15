import Link from "next/link";

const links = [
  ["Draft", "/draft"],
  ["Heroes", "/heroes"],
  ["Meta", "/meta"],
  ["Leaderboard", "/leaderboard"],
  ["Community", "/community"],
] as const;

export function SiteNav() {
  return (
    <nav className="site-nav">
      <Link className="brand" href="/">MLBB CHUNKY</Link>
      <div className="nav-links">
        {links.map(([label, href]) => (
          <Link key={href} href={href}>{label}</Link>
        ))}
      </div>
      <Link className="nav-cta" href="/link-account">Link MLBB</Link>
    </nav>
  );
}
