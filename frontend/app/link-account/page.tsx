"use client";

import { FormEvent, useState } from "react";
import { SiteNav } from "../components/SiteNav";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type Profile = {
  roleId?: number;
  zoneId?: number;
  nickname?: string;
  avatarUrl?: string;
  level?: number;
  rankLevel?: number;
  highestRankLevel?: number;
  registeredCountry?: string;
};

export default function LinkAccountPage() {
  const [roleId, setRoleId] = useState("");
  const [zoneId, setZoneId] = useState("");
  const [code, setCode] = useState("");
  const [codeSent, setCodeSent] = useState(false);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  async function sendCode(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    try {
      const response = await fetch(`${API_BASE}/api/v1/auth/mlbb/verification-code`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ roleId: Number(roleId), zoneId: Number(zoneId) }),
      });
      if (!response.ok) throw new Error(`Could not send code (${response.status})`);
      setCodeSent(true);
      setMessage("Verification code requested. Check your MLBB in-game mail.");
    } catch (cause) {
      setMessage(cause instanceof Error ? cause.message : "Could not send verification code");
    } finally {
      setBusy(false);
    }
  }

  async function verify(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    try {
      const response = await fetch(`${API_BASE}/api/v1/auth/mlbb/verify`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ roleId: Number(roleId), zoneId: Number(zoneId), verificationCode: code }),
      });
      if (!response.ok) throw new Error(`Verification failed (${response.status})`);
      setProfile(await response.json());
      setMessage("MLBB account ownership verified.");
    } catch (cause) {
      setMessage(cause instanceof Error ? cause.message : "Verification failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main>
      <SiteNav />
      <section className="page-header compact">
        <p className="eyebrow">MLBB IDENTITY</p>
        <h1>Link the account you actually play.</h1>
        <p className="lede">Chunky never asks for or stores your MLBB password. Ownership is verified using your User ID, Zone ID and the 4-digit code sent in-game.</p>
      </section>

      <section className="workspace two-column">
        <div className="panel">
          <form className="form-panel" onSubmit={sendCode}>
            <label>User / Role ID<input inputMode="numeric" value={roleId} onChange={(e) => setRoleId(e.target.value)} required /></label>
            <label>Zone / Server ID<input inputMode="numeric" value={zoneId} onChange={(e) => setZoneId(e.target.value)} required /></label>
            <button disabled={busy || !roleId || !zoneId} type="submit">{busy ? "Requesting…" : "Send in-game code"}</button>
          </form>

          {codeSent && (
            <form className="form-panel verification-form" onSubmit={verify}>
              <label>4-digit verification code<input maxLength={4} pattern="\d{4}" inputMode="numeric" value={code} onChange={(e) => setCode(e.target.value.replace(/\D/g, ""))} placeholder="1234" required /></label>
              <button disabled={busy || code.length !== 4} type="submit">Verify account</button>
            </form>
          )}
          {message && <p className="status-text">{message}</p>}
        </div>

        <div className="panel account-preview">
          <span className="card-kicker">Verified profile</span>
          {profile ? (
            <>
              <h2>{profile.nickname ?? "MLBB Player"}</h2>
              <div className="profile-list">
                <span>Role ID <strong>{profile.roleId ?? roleId}</strong></span>
                <span>Zone ID <strong>{profile.zoneId ?? zoneId}</strong></span>
                <span>Level <strong>{profile.level ?? "—"}</strong></span>
                <span>Rank <strong>{profile.rankLevel ?? "—"}</strong></span>
                <span>Highest rank <strong>{profile.highestRankLevel ?? "—"}</strong></span>
                <span>Country <strong>{profile.registeredCountry ?? "—"}</strong></span>
              </div>
            </>
          ) : (
            <><h2>No account linked yet.</h2><p>When verification succeeds, your normalized MLBB profile will appear here. Chunky will later use this verified identity for popularity rankings and community reputation.</p></>
          )}
        </div>
      </section>
    </main>
  );
}
