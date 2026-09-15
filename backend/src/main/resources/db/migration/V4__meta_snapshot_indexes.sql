create index if not exists idx_meta_scope_period_captured
    on hero_meta_snapshot(rank_scope, period_days, captured_at desc);
