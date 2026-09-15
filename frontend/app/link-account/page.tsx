"use client";

import { FormEvent, useEffect, useState } from "react";
import { SiteNav } from "../components/SiteNav";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type Profile = {
  userId?: string;
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

  useEffect(() => {
    fetch(`${API_BASE}/api/v1/auth/mlbb/me`, { credentials: "include" })
      .then(async (response) => {
        if (response.status === 401) return null;
        if (!response.ok) throw new Error(`Could not load session (${response.status})`);
        return response.json() as Promise<Profile>;
      })
      .then((currentProfile) => {
        if (!currentProfile) return;
        setProfile(currentProfile);
        setRoleId(String(currentProfile.roleId ?? ""));
        setZoneId(String(currentProfile.zoneId ?? ""));
      })
      .catch((cause) => setMessage(cause instanceof Error ? cause.message : "Could not load session"));
  }, []);

  async function sendCode(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    try {
      const response = await fetch(`${API_BASE}/api/v1/auth/mlbb/verification-code`, {
        method: "POST",
        credentials: "include",
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
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ roleId: Number(roleId), zoneId: Number(zoneId), verificationCode: code }),
      });
      if (!response.ok) throw new Error(`Verification failed (${response.status})`);
      setProfile(await response.json());
      setCode("");
      setCodeSent(false);
      setMessage("MLBB account verified. You are now signed in to Chunky.");
    } catch (cause) {
      setMessage(cause instanceof Error ? cause.message : "Verification failed");
    } finally {
      setBusy(false);
    }
  }

  async function logout() {
    setBusy(true);
    setMessage("");
    try {
      const response = await fetch(`${API_BASE}/api/v1/auth/mlbb/logout`, {
        method: "POST",
        credentials: "include",
      });
      if (!response.ok) throw new Error(`Could not sign out (${response.status})`);
      setProfile(null);
      setCodeSent(false);
      setCode("");
      setMessage("Signed out of Chunky. Your verified account record remains saved.");
    } catch (cause) {
      setMessage(cause instanceof Error ? cause.message : "Could not sign out");
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
          {profile && <button disabled={busy} type="button" onClick={logout}>Sign out of Chunky</button>}
          {message && <p className="status-text">{message}</p>}
        </div>

        <div className="panel account-preview">
          <span className="card-kicker">Verified profile</span>
          {profile ? (
            <>
              {profile.avatarUrl && <img src={profile.avatarUrl} alt={`${profile.nickname ?? "MLBB Player"} avatar`} width={96} height={96} />}
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
            <><h2>No account linked yet.</h2><p>After verification, Chunky saves the normalized MLBB profile and keeps you signed in with an HTTP-only session cookie.</p></>
          )}
        </div>
      </section>
    </main>
  );
}
