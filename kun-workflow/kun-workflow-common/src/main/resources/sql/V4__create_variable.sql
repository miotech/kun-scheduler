DROP TABLE IF EXISTS kun_wf_variable;

CREATE TABLE kun_wf_variable (
                                  key VARCHAR(256) NOT NULL,
                                  value TEXT NOT NULL,
                                  is_encrypted BOOLEAN NOT NULL,
                                  PRIMARY KEY (key)
);


create unique index kun_wf_variable_key_uidx
    on kun_wf_variable (key);
