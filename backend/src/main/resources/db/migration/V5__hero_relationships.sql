create table hero_relationship (
    subject_hero_id bigint not null references hero(id) on delete cascade,
    related_hero_id bigint not null references hero(id) on delete cascade,
    relationship_type varchar(16) not null,
    rank_scope varchar(64) not null,
    period_days integer not null,
    impact numeric(10,8),
    updated_at timestamptz not null default now(),
    primary key (
        subject_hero_id,
        related_hero_id,
        relationship_type,
        rank_scope,
        period_days
    ),
    constraint chk_hero_relationship_type
        check (relationship_type in ('COUNTER', 'SYNERGY')),
    constraint chk_hero_relationship_distinct
        check (subject_hero_id <> related_hero_id)
);

create index idx_hero_relationship_subject
    on hero_relationship(subject_hero_id, relationship_type, rank_scope, period_days);

create index idx_hero_relationship_related
    on hero_relationship(related_hero_id, relationship_type, rank_scope, period_days);
