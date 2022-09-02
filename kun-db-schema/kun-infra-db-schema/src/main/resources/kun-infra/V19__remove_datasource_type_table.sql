drop table IF EXISTS kun_mt_datasource_type;
ALTER TABLE public.kun_mt_datasource DROP COLUMN IF  EXISTS type_id;
ALTER TABLE public.kun_mt_datasource_type_fields DROP COLUMN IF  EXISTS type_id;