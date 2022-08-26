
CREATE TABLE IF NOT EXISTS kun_mt_filter_rule (
    "filter_type" varchar  not NULL,
    "positive" bool NULL DEFAULT true,
    "rule" varchar  not NULL,
    "updated_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" bool NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS kun_mt_filter_rule_index ON kun_mt_filter_rule (filter_type,positive,rule,deleted);
