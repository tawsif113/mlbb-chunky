alter table app_user
    add column if not exists level integer;

create table app_session (
    id uuid primary key,
    user_id uuid not null references app_user(id) on delete cascade,
    token_hash char(64) not null unique,
    created_at timestamptz not null default now(),
    last_seen_at timestamptz not null default now(),
    expires_at timestamptz not null,
    revoked_at timestamptz
);

create index idx_app_session_user on app_session(user_id);
create index idx_app_session_active on app_session(token_hash, expires_at) where revoked_at is null;
