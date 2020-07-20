alter table kun_dq_case
    drop constraint kun_dq_case_kun_dq_case_datasource_template_id_fk;

alter table kun_dq_case
    add constraint kun_dq_case_kun_dq_case_datasource_template_id_fk
        foreign key (template_id) references kun_dq_case_datasource_template
            on update cascade on delete set null;

alter table kun_dq_case
    drop constraint kun_dq_case_kun_mt_dataset_gid_fk;

alter table kun_dq_case
    drop column dataset_id;