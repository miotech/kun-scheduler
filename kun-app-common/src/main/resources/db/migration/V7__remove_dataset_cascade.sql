alter table kun_dq_case_associated_dataset drop constraint kun_dq_case_associated_dataset_kun_mt_dataset_gid_fk;

alter table kun_dq_case_associated_dataset
    add constraint kun_dq_case_associated_dataset_kun_mt_dataset_gid_fk
        foreign key (dataset_id) references kun_mt_dataset;

alter table kun_dq_case_associated_dataset_field drop constraint kun_dq_case_associated_dataset_field_dataset_field_id_fk;

alter table kun_dq_case_associated_dataset_field
    add constraint kun_dq_case_associated_dataset_field_dataset_field_id_fk
        foreign key (dataset_field_id) references kun_mt_dataset_field;

alter table kun_mt_glossary_to_dataset_ref drop constraint kun_mt_glossary_to_dataset_ref_kun_mt_dataset_gid_fk;

alter table kun_mt_glossary_to_dataset_ref
    add constraint kun_mt_glossary_to_dataset_ref_kun_mt_dataset_gid_fk
        foreign key (dataset_id) references kun_mt_dataset;