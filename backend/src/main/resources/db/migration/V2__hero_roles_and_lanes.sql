create table hero_role (
    hero_id bigint not null references hero(id) on delete cascade,
    role varchar(32) not null,
    primary key (hero_id, role),
    constraint chk_hero_role_value check (role in ('TANK', 'FIGHTER', 'ASSASSIN', 'MAGE', 'MARKSMAN', 'SUPPORT'))
);

create table hero_lane (
    hero_id bigint not null references hero(id) on delete cascade,
    lane varchar(32) not null,
    primary key (hero_id, lane),
    constraint chk_hero_lane_value check (lane in ('EXP', 'GOLD', 'MID', 'JUNGLE', 'ROAM'))
);

create index idx_hero_role_role on hero_role(role, hero_id);
create index idx_hero_lane_lane on hero_lane(lane, hero_id);
