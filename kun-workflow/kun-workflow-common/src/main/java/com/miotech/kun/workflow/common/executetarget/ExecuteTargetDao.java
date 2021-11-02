package com.miotech.kun.workflow.common.executetarget;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.workflow.core.model.executetarget.ExecuteTarget;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Singleton
public class ExecuteTargetDao {


    private final DatabaseOperator dbOperator;

    private final String[] targetCols = {"name","properties","create_at","update_at"};

    private final String[] fetchTargetCols = {"id","name","properties","create_at","update_at"};

    private final String[] updateTargetCols = {"name","properties","update_at"};

    private final String TARGET_TABLE = "kun_wf_target";

    @Inject
    public ExecuteTargetDao(DatabaseOperator dbOperator){
        this.dbOperator = dbOperator;
    }

    public ExecuteTarget createExecuteTarget(ExecuteTarget executeTarget){
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(targetCols)
                .into(TARGET_TABLE)
                .asPrepared()
                .getSQL();
        dbOperator.update(sql,
                executeTarget.getName(),
                JSONUtils.toJsonString(executeTarget.getProperties()),
                DateTimeUtils.now(),
                DateTimeUtils.now()
        );
        return executeTarget;
    }
    public ExecuteTarget updateExecuteTarget(ExecuteTarget executeTarget){
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TARGET_TABLE)
                .set(updateTargetCols)
                .where("id = ?")
                .asPrepared()
                .getSQL();
        dbOperator.update(sql,
                executeTarget.getName(),
                JSONUtils.toJsonString(executeTarget.getProperties()),
                DateTimeUtils.now(),
                executeTarget.getId()
        );
        return executeTarget;
    }
    public ExecuteTarget fetchExecuteTarget(Long id){
        String sql = DefaultSQLBuilder.newBuilder()
                .select(fetchTargetCols)
                .from(TARGET_TABLE)
                .where("id = ?")
                .asPrepared()
                .getSQL();
        List<ExecuteTarget> executeTargets = dbOperator.fetchAll(sql,ExecuteTargetMapper.INSTANCE,id);
        if (executeTargets.size() == 0){
            return null;
        }
        return executeTargets.get(0);
    }
    public ExecuteTarget fetchExecuteTarget(String name){
        String sql = DefaultSQLBuilder.newBuilder()
                .select(fetchTargetCols)
                .from(TARGET_TABLE)
                .where("name = ?")
                .asPrepared()
                .getSQL();
        List<ExecuteTarget> executeTargets = dbOperator.fetchAll(sql,ExecuteTargetMapper.INSTANCE,name);
        if (executeTargets.size() == 0){
            return null;
        }
        return executeTargets.get(0);
    }

    public List<ExecuteTarget> fetchExecuteTargets(){
        String sql = DefaultSQLBuilder.newBuilder()
                .select(fetchTargetCols)
                .from(TARGET_TABLE)
                .orderBy("id asc")
                .asPrepared()
                .getSQL();
        return dbOperator.fetchAll(sql,ExecuteTargetMapper.INSTANCE);

    }

    private static class ExecuteTargetMapper implements ResultSetMapper<ExecuteTarget> {

        public static final ResultSetMapper<ExecuteTarget> INSTANCE = new ExecuteTargetMapper();

        @Override
        public ExecuteTarget map(ResultSet rs) throws SQLException {
            return ExecuteTarget.newBuilder()
                    .withId(rs.getLong("id"))
                    .withName(rs.getString("name"))
                    .withProperties(JSONUtils.jsonStringToMap(rs.getString("properties")))
                    .build();
        }
    }
}
