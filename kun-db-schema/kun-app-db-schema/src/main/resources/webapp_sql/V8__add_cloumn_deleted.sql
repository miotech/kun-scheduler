ALTER TABLE kun_mt_glossary ADD deleted bool NOT NULL DEFAULT false;

ALTER TABLE kun_mt_glossary_to_dataset_ref ADD update_user varchar(256)   NULL;
ALTER TABLE kun_mt_glossary_to_dataset_ref ADD update_time timestamp  NULL;
ALTER TABLE kun_mt_glossary_to_dataset_ref ADD deleted bool NOT NULL DEFAULT false;

