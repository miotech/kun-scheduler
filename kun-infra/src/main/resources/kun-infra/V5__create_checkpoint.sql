DROP TABLE IF EXISTS kun_wf_checkpoint;

CREATE TABLE kun_wf_checkpoint (
    id BIGSERIAL PRIMARY KEY,
    checkpoint_tick VARCHAR(64) NOT NULL
);