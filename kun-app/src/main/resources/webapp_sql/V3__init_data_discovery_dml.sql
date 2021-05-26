INSERT INTO kun_mt_datasource_type (id, name)
VALUES (1, 'AWS'),
       (2, 'MongoDB'),
       (3, 'PostgreSQL'),
       (4, 'Elasticsearch'),
       (5, 'Arango')
ON CONFLICT (id) DO NOTHING;

INSERT INTO kun_mt_datasource_type_fields (id, type_id, name, sequence_order, format, require)
VALUES (1, 2, 'host', 1, 'INPUT', true),
       (2, 2, 'port', 2, 'NUMBER_INPUT', true),
       (3, 2, 'username', 3, 'INPUT', false),
       (4, 2, 'password', 4, 'PASSWORD', false),
       (5, 3, 'host', 1, 'INPUT', true),
       (6, 3, 'port', 2, 'NUMBER_INPUT', true),
       (7, 3, 'username', 3, 'INPUT', false),
       (8, 3, 'password', 4, 'PASSWORD', false),
       (9, 4, 'host', 1, 'INPUT', true),
       (10, 4, 'port', 2, 'NUMBER_INPUT', true),
       (11, 4, 'username', 3, 'INPUT', false),
       (12, 4, 'password', 4, 'PASSWORD', false),
       (13, 5, 'host', 1, 'INPUT', true),
       (14, 5, 'port', 2, 'NUMBER_INPUT', true),
       (15, 5, 'username', 3, 'INPUT', false),
       (16, 5, 'password', 4, 'PASSWORD', false),
       (17, 1, 'glueAccessKey', 1, 'INPUT', false),
       (18, 1, 'glueSecretKey', 2, 'INPUT', false),
       (19, 1, 'glueRegion', 3, 'INPUT', false),
       (20, 1, 'athenaUrl', 4, 'INPUT', false),
       (21, 1, 'athenaUsername', 5, 'INPUT', false),
       (22, 1, 'athenaPassword', 6, 'PASSWORD', false)
ON CONFLICT (id) DO NOTHING;
