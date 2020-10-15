package com.miotech.kun.workflow.common.tick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.workflow.core.model.common.Tick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class TickDao {

    private static final Logger logger = LoggerFactory.getLogger(TickDao.class);

    private static final String CHECKPOINT_TABLE_NAME = "kun_wf_checkpoint";

    private static final String CHECKPOINT_TICK = "checkpoint_tick";

    private static final String CHECKPOINT_TICK_ID = "id";


    private static final ResultSetMapper<Tick> tickMapper =
            rs -> new Tick(rs.getString(CHECKPOINT_TICK));

    @Inject
    private DatabaseOperator dbOperator;

    public Tick getLatestCheckPoint(){
        String sql = DefaultSQLBuilder.newBuilder()
                .select(CHECKPOINT_TICK)
                .from(CHECKPOINT_TABLE_NAME)
                .orderBy(CHECKPOINT_TICK_ID)
                .asPrepared()
                .getSQL();
        List<Tick> tickList =  dbOperator.fetchAll(sql,tickMapper);
        if(tickList.size() == 0){
            return null;
        }else {
            return tickList.get(0);
        }
    }

    public boolean saveCheckPoint(Tick tick){
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(CHECKPOINT_TICK)
                .into(CHECKPOINT_TABLE_NAME)
                .asPrepared()
                .getSQL();
        boolean result = dbOperator.update(sql,tick.getTime()) == 1;
        return result;
    }

}
