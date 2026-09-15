"use client";

import { useMemo, useState } from "react";

export type Hero = {
  id: number;
  name: string;
  imageUrl?: string;
  roles: string[];
  lanes: string[];
  winRate: number;
  pickRate: number;
  banRate: number;
  strongAgainst: number[];
  synergizesWith: number[];
};

type HeroPickerProps = {
  label: string;
  heroes: Hero[];
  selectedIds: number[];
  blockedIds: Set<number>;
  onChange: (ids: number[]) => void;
  placeholder: string;
  loading?: boolean;
};

export function HeroPicker({
  label,
  heroes,
  selectedIds,
  blockedIds,
  onChange,
  placeholder,
  loading = false,
}: HeroPickerProps) {
  const [query, setQuery] = useState("");
  const selectedSet = useMemo(() => new Set(selectedIds), [selectedIds]);

  const selectedHeroes = useMemo(
    () => selectedIds.map((id) => heroes.find((hero) => hero.id === id)).filter((hero): hero is Hero => Boolean(hero)),
    [heroes, selectedIds],
  );

  const suggestions = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return [];

    return heroes
      .filter((hero) => !selectedSet.has(hero.id) && !blockedIds.has(hero.id))
      .filter((hero) => {
        const searchable = [hero.name, ...hero.roles, ...hero.lanes].join(" ").toLowerCase();
        return searchable.includes(normalized);
      })
      .slice(0, 10);
  }, [blockedIds, heroes, query, selectedSet]);

  function addHero(heroId: number) {
    if (selectedSet.has(heroId) || blockedIds.has(heroId)) return;
    onChange([...selectedIds, heroId]);
    setQuery("");
  }

  function removeHero(heroId: number) {
    onChange(selectedIds.filter((id) => id !== heroId));
  }

  return (
    <div className="hero-picker-field">
      <div className="hero-picker-label-row">
        <span>{label}</span>
        <small>{selectedIds.length} selected</small>
      </div>

      {selectedHeroes.length > 0 && (
        <div className="selected-heroes">
          {selectedHeroes.map((hero) => (
            <button
              className="selected-hero"
              key={hero.id}
              type="button"
              onClick={() => removeHero(hero.id)}
              title={`Remove ${hero.name}`}
            >
              {hero.imageUrl ? <img src={hero.imageUrl} alt="" /> : <span className="hero-avatar-fallback">{hero.name[0]}</span>}
              <span>{hero.name}</span>
              <strong aria-hidden="true">×</strong>
            </button>
          ))}
        </div>
      )}

      <div className="hero-search-wrap">
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={loading ? "Loading hero catalog…" : placeholder}
          disabled={loading}
          autoComplete="off"
          aria-label={`${label} hero search`}
        />

        {query.trim() && !loading && (
          <div className="hero-suggestions">
            {suggestions.length > 0 ? (
              suggestions.map((hero) => (
                <button className="hero-suggestion" key={hero.id} type="button" onClick={() => addHero(hero.id)}>
                  {hero.imageUrl ? <img src={hero.imageUrl} alt="" /> : <span className="hero-avatar-fallback">{hero.name[0]}</span>}
                  <span className="hero-suggestion-copy">
                    <strong>{hero.name}</strong>
                    <small>{[...hero.roles, ...hero.lanes].join(" · ") || `Hero #${hero.id}`}</small>
                  </span>
                  <span className="hero-suggestion-id">#{hero.id}</span>
                </button>
              ))
            ) : (
              <div className="hero-suggestion-empty">No available hero matches “{query.trim()}”.</div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
