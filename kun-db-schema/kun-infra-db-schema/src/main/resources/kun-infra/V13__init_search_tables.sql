

CREATE TABLE IF NOT EXISTS kun_mt_universal_search (

gid bigint  not NULL,

resource_type varchar  not NULL,

"name" varchar NULL,

description varchar NULL,

resource_attribute jsonb NULL,

search_ts tsvector NULL,

update_time timestamp  NULL,

deleted bool NULL DEFAULT false

);

ALTER TABLE kun_mt_universal_search ADD CONSTRAINT kun_mt_search_unq UNIQUE ( gid,resource_type);



UPDATE kun_mt_universal_search SET search_ts =
    setweight(to_tsvector('english',coalesce(name,'')), 'A')    ||
    setweight(to_tsvector('english',coalesce(description,'')), 'B')  ||
    setweight(to_tsvector('english',coalesce((select  string_agg(distinct(value), ',') from jsonb_each_text(resource_attribute)),'')), 'C');

create index on kun_mt_universal_search using gin(search_ts) ;


CREATE FUNCTION search_search_ts_trigger() RETURNS trigger AS $$
begin
 new.search_ts =
    setweight(to_tsvector('english',coalesce(new.name,'')), 'A')    ||
    setweight(to_tsvector('english',coalesce(new.description,'')), 'B')  ||
    setweight(to_tsvector('english',coalesce((select  string_agg(distinct(value), ',') from jsonb_each_text(new.resource_attribute)),'')), 'C');
   return  new;
end
$$ LANGUAGE plpgsql;


CREATE TRIGGER tsvector_update BEFORE INSERT OR UPDATE
    ON kun_mt_universal_search FOR EACH ROW EXECUTE FUNCTION search_search_ts_trigger();

