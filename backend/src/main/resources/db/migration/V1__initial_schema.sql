create table app_user (
    id uuid primary key,
    mlbb_role_id bigint not null,
    mlbb_zone_id bigint not null,
    nickname varchar(128) not null,
    avatar_url text,
    registered_country varchar(16),
    rank_level integer,
    highest_rank_level integer,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uq_user_mlbb_account unique (mlbb_role_id, mlbb_zone_id)
);

create table hero (
    id bigint primary key,
    name varchar(128) not null unique,
    image_url text,
    updated_at timestamptz not null default now()
);

create table user_hero_preference (
    user_id uuid not null references app_user(id) on delete cascade,
    hero_id bigint not null references hero(id) on delete cascade,
    favorite boolean not null default false,
    matches_played integer,
    imported_at timestamptz,
    primary key (user_id, hero_id)
);

create table hero_meta_snapshot (
    id bigserial primary key,
    hero_id bigint not null references hero(id) on delete cascade,
    rank_scope varchar(64) not null,
    period_days integer not null,
    pick_rate numeric(8,6),
    ban_rate numeric(8,6),
    win_rate numeric(8,6),
    captured_at timestamptz not null
);

create table community_post (
    id uuid primary key,
    author_id uuid not null references app_user(id) on delete cascade,
    hero_id bigint references hero(id) on delete set null,
    title varchar(180) not null,
    body text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table community_comment (
    id uuid primary key,
    post_id uuid not null references community_post(id) on delete cascade,
    author_id uuid not null references app_user(id) on delete cascade,
    parent_comment_id uuid references community_comment(id) on delete cascade,
    body text not null,
    created_at timestamptz not null default now()
);

create index idx_meta_hero_captured on hero_meta_snapshot(hero_id, captured_at desc);
create index idx_post_hero_created on community_post(hero_id, created_at desc);
create index idx_comment_post_created on community_comment(post_id, created_at);
