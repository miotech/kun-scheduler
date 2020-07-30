CREATE TABLE kun_dp_task_definition (
                                        id BIGINT NOT NULL,
                                        definition_id BIGINT NOT NULL,
                                        name VARCHAR(1024) NOT NULL,
                                        task_template_name VARCHAR(1024) NOT NULL,
                                        task_payload JSONB,
                                        creator BIGINT NOT NULL,
                                        owner BIGINT NOT NULL,
                                        is_archived BOOLEAN NOT NULL,
                                        last_modifier BIGINT NOT NULL,
                                        create_time TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                        update_time TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                        created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        PRIMARY KEY (id)
);

CREATE TABLE kun_dp_task_datasets (
                                      id BIGINT NOT NULL,
                                      definition_id BIGINT NOT NULL,
                                      datastore_id BIGINT NOT NULL,
                                      dataset_name VARCHAR(255) NOT NULL,
                                      created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                      PRIMARY KEY (id)
);

CREATE TABLE kun_dp_data_store (
                                      id BIGINT NOT NULL,
                                      name VARCHAR(255) NOT NULL,
                                      url VARCHAR(255) NOT NULL,
                                      username VARCHAR(255) NOT NULL,
                                      password VARCHAR(255) NOT NULL,
                                      type VARCHAR(255) NOT NULL,
                                      created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                      PRIMARY KEY (id)
);

CREATE TABLE kun_dp_task_try (
                                 id BIGINT NOT NULL,
                                 definition_id BIGINT NOT NULL,
                                 wf_task_id BIGINT NOT NULL,
                                 wf_task_run_id BIGINT NOT NULL,
                                 task_config JSONB,
                                 creator BIGINT NOT NULL,
                                 created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                 PRIMARY KEY (id)
);

CREATE TABLE kun_dp_deployed_task (
                                      id BIGINT NOT NULL,
                                      definition_id BIGINT NOT NULL,
                                      name VARCHAR(1024) NOT NULL,
                                      task_template_name VARCHAR(1024) NOT NULL,
                                      wf_task_id BIGINT NOT NULL,
                                      owner BIGINT NOT NULL,
                                      commit_id BIGINT NOT NULL,
                                      is_archived BOOLEAN NOT NULL,
                                      created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                      PRIMARY KEY (id)
);

CREATE TABLE kun_dp_task_commit (
                                    id BIGINT NOT NULL,
                                    task_def_id BIGINT NOT NULL,
                                    version	 INT NOT NULL,
                                    message TEXT,
                                    snapshot JSONB,
                                    committer BIGINT NOT NULL ,
                                    committed_at TIMESTAMP NOT NULL,
                                    commit_type VARCHAR(255) NOT NULL,
                                    commit_status VARCHAR(255) NOT NULL,
                                    is_latest BOOLEAN DEFAULT  FALSE,
                                    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                    PRIMARY KEY (id)
);

CREATE TABLE kun_dp_deploy (
                               id BIGINT NOT NULL,
                               name VARCHAR(1024) NOT NULL,
                               creator BIGINT NOT NULL ,
                               submitted_at TIMESTAMP NOT NULL,
                               deployer BIGINT NULL ,
                               deployed_at TIMESTAMP NULL,
                               status VARCHAR(255) NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                               PRIMARY KEY (id)
);

CREATE TABLE kun_dp_deploy_commits (
                                       deploy_id BIGINT NOT NULL,
                                       commit_id BIGINT NOT NULL,
                                       deploy_status VARCHAR(255) NOT NULL,
                                       created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP,
                                       PRIMARY KEY (deploy_id, commit_id)
);