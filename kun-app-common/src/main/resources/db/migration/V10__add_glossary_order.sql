alter table kun_mt_glossary add prev_id bigint;

update kun_mt_glossary kmg
set prev_id = kmg_next.next_id
from (select id,
             lead(id) over (order by parent_id, create_time)        as next_id,
             lead(parent_id) over (order by parent_id, create_time) as next_parent_id
      from kun_mt_glossary
      order by parent_id, create_time) kmg_next
where kmg.id = kmg_next.id
  and (kmg.parent_id = kmg_next.next_parent_id or kmg.parent_id is null);