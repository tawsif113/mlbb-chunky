const features = [
  {
    title: "Draft Assistant",
    text: "Set your lane, allies and enemy picks. Chunky ranks the best available heroes and explains every score.",
  },
  {
    title: "Community Meta",
    text: "See what verified Chunky players actually favorite and play — globally or by rank, lane and region.",
  },
  {
    title: "Hero Rooms",
    text: "Discuss matchups, builds, patches and draft decisions with players around each hero.",
  },
  {
    title: "MLBB-linked identity",
    text: "Link an account with User ID, Zone ID and an in-game verification code instead of creating another password.",
  },
];

export default function Home() {
  return (
    <main>
      <nav>
        <strong>MLBB CHUNKY</strong>
        <span>Draft · Meta · Community</span>
      </nav>

      <section className="hero">
        <p className="eyebrow">DRAFT WITH CONTEXT</p>
        <h1>Know what to pick.<br />Know why it works.</h1>
        <p className="lede">
          A Mobile Legends companion built around draft decisions, transparent recommendations,
          verified-player popularity and community knowledge.
        </p>
        <div className="actions">
          <button>Open Draft Assistant</button>
          <button className="secondary">Explore Heroes</button>
        </div>
      </section>

      <section className="grid">
        {features.map((feature) => (
          <article key={feature.title}>
            <h2>{feature.title}</h2>
            <p>{feature.text}</p>
          </article>
        ))}
      </section>

      <footer>
        Mobile Legends: Bang Bang and related marks belong to their respective owners. MLBB Chunky is an unofficial community project. Optional account-data integration is powered by {" "}
        <a href="https://arena.rone.dev" target="_blank" rel="noreferrer">Rone Arena</a> when enabled.
      </footer>
    </main>
  );
}
