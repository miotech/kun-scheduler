package com.miotech.kun.datadiscovery.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.model.PageResult;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.RefColumn;
import com.miotech.kun.datadiscovery.model.enums.ConstraintType;
import com.miotech.kun.datadiscovery.model.enums.RefTableVersionStatus;
import com.miotech.kun.datadiscovery.util.BasicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-30 15:04
 **/
@Repository
@Slf4j
@RequiredArgsConstructor
public class RefTableVersionRepository extends BaseRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final String TABLE_RTVI_NAME = "kun_mt_ref_table_version_info";
    private static final String TABLE_RTVI_ALIAS = "kmrtvi";
    private static final String RTVI_VERSION_ID = "version_id";
    private static final String RTVI_VERSION_NUMBER = "version_number";
    private static final String RTVI_VERSION_DESCRIPTION = "version_description";
    private static final String RTVI_TABLE_ID = "table_id";
    private static final String RTVI_TABLE_NAME = "table_name";
    private static final String RTVI_DATABASE_NAME = "database_name";
    private static final String RTVI_DATA_PATH = "data_path";
    private static final String RTVI_GLOSSARY_LIST = "glossary_list";
    private static final String RTVI_OWNER_LIST = "owner_list";
    private static final String RTVI_REF_TABLE_COLUMNS = "ref_table_columns";
    private static final String RTVI_REF_TABLE_CONSTRAINTS = "ref_table_constraints";
    private static final String RTVI_PUBLISHED = "published";
    private static final String RTVI_STATUS = "status";
    private static final String RTVI_START_TIME = "start_time";
    private static final String RTVI_END_TIME = "end_time";
    private static final String RTVI_CREATE_USER = "create_user";
    private static final String RTVI_CREATE_TIME = "create_time";
    private static final String RTVI_UPDATE_USER = "update_user";
    private static final String RTVI_UPDATE_TIME = "update_time";
    private static final String RTVI_DELETED = "deleted";
    private static final String RTVI_DATASET_ID = "dataset_id";
    private static final String[] COLUMNS = {RTVI_VERSION_ID, RTVI_VERSION_NUMBER, RTVI_VERSION_DESCRIPTION, RTVI_TABLE_ID, RTVI_TABLE_NAME, RTVI_DATABASE_NAME,
            RTVI_DATA_PATH, RTVI_GLOSSARY_LIST, RTVI_OWNER_LIST, RTVI_REF_TABLE_COLUMNS, RTVI_REF_TABLE_CONSTRAINTS, RTVI_PUBLISHED, RTVI_STATUS, RTVI_START_TIME, RTVI_END_TIME, RTVI_CREATE_USER
            , RTVI_CREATE_TIME, RTVI_UPDATE_USER, RTVI_UPDATE_TIME, RTVI_DELETED, RTVI_DATASET_ID};

    @Transactional(rollbackFor = Exception.class)
    public RefTableVersionInfo create(RefTableVersionInfo refTableVersionInfo) {
        List<Object> params = Lists.newArrayList();
        params.add(refTableVersionInfo.getVersionId());
        params.add(refTableVersionInfo.getVersionNumber());
        params.add(StringUtils.stripToEmpty(refTableVersionInfo.getVersionDescription()));
        params.add(refTableVersionInfo.getTableId());
        params.add(refTableVersionInfo.getTableName());
        params.add(refTableVersionInfo.getDatabaseName());
        params.add(refTableVersionInfo.getDataPath());
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getGlossaryList()));
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getOwnerList()));
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getRefTableColumns()));
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getRefTableConstraints()));
        params.add(refTableVersionInfo.getPublished());
        params.add(refTableVersionInfo.getStatus().name());
        params.add(refTableVersionInfo.getStartTime());
        params.add(refTableVersionInfo.getEndTime());
        params.add(refTableVersionInfo.getCreateUser());
        params.add(refTableVersionInfo.getCreateTime());
        params.add(refTableVersionInfo.getUpdateUser());
        params.add(refTableVersionInfo.getUpdateTime());
        params.add(false);
        params.add(refTableVersionInfo.getDatasetId());
        String sql = DefaultSQLBuilder.newBuilder().insert(COLUMNS).into(TABLE_RTVI_NAME).asPrepared().getSQL();
        jdbcTemplate.update(sql, params.toArray());
        return refTableVersionInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefTableVersionInfo update(RefTableVersionInfo refTableVersionInfo) {
        List<Object> params = Lists.newArrayList();

        String[] setOptions = {RTVI_VERSION_NUMBER, RTVI_VERSION_DESCRIPTION, RTVI_TABLE_ID, RTVI_TABLE_NAME, RTVI_DATABASE_NAME,
                RTVI_DATA_PATH, RTVI_GLOSSARY_LIST, RTVI_OWNER_LIST, RTVI_REF_TABLE_COLUMNS,
                RTVI_REF_TABLE_CONSTRAINTS, RTVI_PUBLISHED, RTVI_STATUS, RTVI_START_TIME, RTVI_END_TIME, RTVI_UPDATE_USER, RTVI_UPDATE_TIME, RTVI_DELETED, RTVI_DATASET_ID};
        params.add(refTableVersionInfo.getVersionNumber());
        params.add(StringUtils.stripToEmpty(refTableVersionInfo.getVersionDescription()));
        params.add(refTableVersionInfo.getTableId());
        params.add(refTableVersionInfo.getTableName());
        params.add(refTableVersionInfo.getDatabaseName());
        params.add(refTableVersionInfo.getDataPath());
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getGlossaryList()));
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getOwnerList()));
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getRefTableColumns()));
        params.add(JSONUtils.toJsonString(refTableVersionInfo.getRefTableConstraints()));
        params.add(refTableVersionInfo.getPublished());
        params.add(refTableVersionInfo.getStatus().name());
        params.add(refTableVersionInfo.getStartTime());
        params.add(refTableVersionInfo.getEndTime());
        params.add(refTableVersionInfo.getUpdateUser());
        params.add(refTableVersionInfo.getUpdateTime());
        params.add(refTableVersionInfo.isDeleted());
        params.add(refTableVersionInfo.getDatasetId());

        String[] options = {RTVI_VERSION_ID};
        params.add(refTableVersionInfo.getVersionId());

        update(params, setOptions, options);
        return refTableVersionInfo;
    }

    private void update(List<Object> params, String[] setOptions, String[] filter) {
        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_RTVI_NAME).asPrepared().set(setOptions).where(and(filter)).getSQL();
        jdbcTemplate.update(sql, params.toArray());
    }

    private RefTableVersionInfo selectOne(ImmutableMap<String, Object> params) {
        List<RefTableVersionInfo> query = selectList(params);
        if (CollectionUtils.isEmpty(query)) {
            return null;
        }
        if (query.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, query.size());
        }
        return query.get(0);
    }

    private List<RefTableVersionInfo> selectList(ImmutableMap<String, Object> params) {
        Object[] keys = params.keySet().toArray();
        Object[] values = Arrays.stream(keys).map(params::get).toArray();
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS)
                .from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS).where(and(keys)).getSQL();
        List<RefTableVersionInfo> query = jdbcTemplate.query(sql, RefDataVersionMapper.INSTANCE, values);
        if (CollectionUtils.isEmpty(query)) {
            return Lists.newArrayList();
        }
        return query;
    }

    private static String and(Object[] keys) {
        StringJoiner and = new StringJoiner(" AND ");
        Arrays.stream(keys).forEach((s -> and.add(String.format("%s=?", s))));
        return and.toString();
    }


    public RefTableVersionInfo findPublishByTableId(Long tableId) {
        if (Objects.isNull(tableId)) {
            log.debug("table id  is null");
            return null;
        }
        return selectOne(ImmutableMap.of(RTVI_TABLE_ID, tableId, RTVI_PUBLISHED, true, RTVI_STATUS, RefTableVersionStatus.PUBLISHED.name(), RTVI_DELETED, false));
    }

    public RefTableVersionInfo findByVersionId(Long versionId) {
        if (Objects.isNull(versionId)) {
            log.debug("version id  is null");
            return null;
        }
        return selectOne(ImmutableMap.of(RTVI_VERSION_ID, versionId));
    }

    public List<RefTableVersionInfo> findByTableId(Long tableId) {
        if (Objects.isNull(tableId)) {
            log.debug("table id  is null");
            return null;
        }
        return selectList(ImmutableMap.of(RTVI_TABLE_ID, tableId, RTVI_DELETED, false));
    }


    public Integer findMaxVersionNumberByTable(Long tableId) {
        String sql = DefaultSQLBuilder.newBuilder().select(String.format("max(%s) as version_number", RTVI_VERSION_NUMBER)).from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS)
                .where(String.format("%s=? and %s = false limit 1", RTVI_TABLE_ID, RTVI_DELETED)).getSQL();
        return jdbcTemplate.query(sql, rs -> rs.next() ? BasicMapper.zeroToNull(rs, RTVI_VERSION_NUMBER, Integer.class) : null, tableId);

    }


    public PageResult<RefTableVersionInfo> pageRefTableInfo(Integer pageNumber, Integer pageSize) {
        Integer count = countRefTableInfo();
        log.debug("count:{}", count);
        if (count <= 0) {
            return new PageResult<>(pageSize, pageNumber, count, Lists.newArrayList());
        }

        String group_sql = DefaultSQLBuilder.newBuilder().select(RTVI_TABLE_ID + " as max_table", String.format("max(coalesce(%s,0)) as max_version_number", RTVI_VERSION_NUMBER)).from(TABLE_RTVI_NAME, "grtvi")
                .where(String.format("%s is null and %s = false", RTVI_END_TIME, RTVI_DELETED))
                .groupBy(RTVI_TABLE_ID).getSQL();
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS)
                .join("inner", String.format("(%s)", group_sql), "temp_table").on("temp_table.max_table=kmrtvi.table_id  and temp_table.max_version_number=COALESCE(kmrtvi.version_number,0)").orderBy(String.format("%s desc ", RTVI_UPDATE_TIME)).getSQL();
        String limitSql = toLimitSql(pageNumber, pageSize);
        sql = sql.concat(limitSql);
        List<RefTableVersionInfo> result = jdbcTemplate.query(sql, RefDataVersionMapper.INSTANCE);
        return new PageResult<>(pageSize, pageNumber, count, result);
    }

    public Integer countRefTableInfo() {
        String sql = DefaultSQLBuilder.newBuilder().select(String.format("count( distinct %s) as count", RTVI_TABLE_ID)).from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS)
                .where(String.format("%s = false limit 1", RTVI_DELETED)).getSQL();
        return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getInt("count") : 0);
    }

    public RefTableVersionInfo findUnPublishedByTable(Long tableId) {
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS)
                .where(and(new String[]{RTVI_TABLE_ID, RTVI_STATUS, RTVI_DELETED})).getSQL();
        List<RefTableVersionInfo> query = jdbcTemplate.query(sql, RefDataVersionMapper.INSTANCE, tableId, RefTableVersionStatus.UNPUBLISHED.name(), false);
        if (CollectionUtils.isEmpty(query)) {
            return null;
        }
        return query.get(0);
    }

    public boolean existsDatabaseTableInfo(String databaseName, String tableName, Long notTableId) {
        String sql = DefaultSQLBuilder.newBuilder().select(String.format("count( distinct %s) as count", RTVI_TABLE_ID)).from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS)
                .where(and(new String[]{RTVI_DATABASE_NAME, RTVI_TABLE_NAME, RTVI_DELETED})).getSQL();
        Integer count;
        if (Objects.isNull(notTableId)) {
            count = jdbcTemplate.query(sql, rs -> rs.next() ? rs.getInt("count") : 0, databaseName, tableName, false);
        } else {
            sql = sql + String.format(" and %s<>?", RTVI_TABLE_ID);
            count = jdbcTemplate.query(sql, rs -> rs.next() ? rs.getInt("count") : 0, databaseName, tableName, false, notTableId);

        }
        return count > 0;
    }

    public boolean hasPublished(Long tableId) {
        String sql = DefaultSQLBuilder.newBuilder().select(String.format("count( distinct %s) as count", RTVI_TABLE_ID)).from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS)
                .where(and(new String[]{RTVI_TABLE_ID, RTVI_STATUS, RTVI_DELETED})).getSQL();
        Integer count = jdbcTemplate.query(sql, rs -> rs.next() ? rs.getInt("count") : 0, tableId, RefTableVersionStatus.PUBLISHED.name(), false);
        return Objects.nonNull(count) && count > 0;
    }


    public void updateDataSetId(String databaseName, String datasetName, Long datasetId) {
        update(ImmutableList.of(datasetId, databaseName, datasetName), new String[]{RTVI_DATASET_ID}, new String[]{RTVI_DATABASE_NAME, RTVI_TABLE_NAME});
    }

    public RefTableVersionInfo findPublishedByDatasetId(Long datasetId) {
        return selectOne(ImmutableMap.of(RTVI_DATASET_ID, datasetId, RTVI_PUBLISHED, true, RTVI_DELETED, false));
    }

    public List<RefTableVersionInfo> findEditGlossaryTableList() {
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS).from(TABLE_RTVI_NAME, TABLE_RTVI_ALIAS)
                .where(String.format("%s in(?,?) and %s is not null and %s=?", RTVI_STATUS, RTVI_DATASET_ID, RTVI_DELETED)).getSQL();
        List<RefTableVersionInfo> query = jdbcTemplate.query(sql, RefDataVersionMapper.INSTANCE,
                RefTableVersionStatus.UNPUBLISHED.name(), RefTableVersionStatus.PUBLISHED.name(), false);
        if (CollectionUtils.isEmpty(query)) {
            return Lists.newArrayList();
        }
        return query;
    }

    private static class RefDataVersionMapper extends BasicMapper<RefTableVersionInfo> {
        public static final RefDataVersionMapper INSTANCE = new RefTableVersionRepository.RefDataVersionMapper();
        private final Logger logger = LoggerFactory.getLogger(RefDataVersionMapper.class);

        @Override
        public RefTableVersionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefTableVersionInfo refTableVersionInfo = new RefTableVersionInfo();
            if (isExistColumn(rs, RTVI_VERSION_ID)) {
                refTableVersionInfo.setVersionId(zeroToNull(rs, RTVI_VERSION_ID, Long.class));
            }
            if (isExistColumn(rs, RTVI_VERSION_NUMBER)) {
                refTableVersionInfo.setVersionNumber(zeroToNull(rs, RTVI_VERSION_NUMBER, Integer.class));
            }
            if (isExistColumn(rs, RTVI_VERSION_DESCRIPTION)) {
                refTableVersionInfo.setVersionDescription(rs.getString(RTVI_VERSION_DESCRIPTION));
            }
            if (isExistColumn(rs, RTVI_TABLE_ID)) {
                refTableVersionInfo.setTableId(zeroToNull(rs, RTVI_TABLE_ID, Long.class));
            }
            if (isExistColumn(rs, RTVI_TABLE_NAME)) {
                refTableVersionInfo.setTableName(rs.getString(RTVI_TABLE_NAME));
            }
            if (isExistColumn(rs, RTVI_DATABASE_NAME)) {
                refTableVersionInfo.setDatabaseName(rs.getString(RTVI_DATABASE_NAME));
            }
            if (isExistColumn(rs, RTVI_DATA_PATH)) {
                refTableVersionInfo.setDataPath(rs.getString(RTVI_DATA_PATH));
            }
            if (isExistColumn(rs, RTVI_GLOSSARY_LIST)) {
                refTableVersionInfo.setGlossaryList(JSONUtils.toJavaObject(rs.getString(RTVI_GLOSSARY_LIST), new TypeToken<List<Long>>() {
                }.getType()));
            }
            if (isExistColumn(rs, RTVI_OWNER_LIST)) {
                refTableVersionInfo.setOwnerList(JSONUtils.toJavaObject(rs.getString(RTVI_OWNER_LIST), new TypeToken<List<String>>() {
                }.getType()));
            }
            if (isExistColumn(rs, RTVI_REF_TABLE_COLUMNS)) {
                refTableVersionInfo.setRefTableColumns(JSONUtils.toJavaObject(rs.getString(RTVI_REF_TABLE_COLUMNS), new TypeToken<LinkedHashSet<RefColumn>>() {
                }.getType()));
            }
            if (isExistColumn(rs, RTVI_REF_TABLE_CONSTRAINTS)) {
                refTableVersionInfo.setRefTableConstraints(JSONUtils.toJavaObject(rs.getString(RTVI_REF_TABLE_CONSTRAINTS), new TypeToken<LinkedHashMap<ConstraintType, Set<String>>>() {
                }.getType()));
            }
            if (isExistColumn(rs, RTVI_PUBLISHED)) {
                refTableVersionInfo.setPublished(rs.getBoolean(RTVI_PUBLISHED));
            }
            if (isExistColumn(rs, RTVI_STATUS)) {
                refTableVersionInfo.setStatus(RefTableVersionStatus.valueOf(rs.getString(RTVI_STATUS)));
            }
            if (isExistColumn(rs, RTVI_START_TIME)) {
                refTableVersionInfo.setStartTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(RTVI_START_TIME)));
            }
            if (isExistColumn(rs, RTVI_END_TIME)) {
                refTableVersionInfo.setEndTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(RTVI_END_TIME)));
            }
            if (isExistColumn(rs, RTVI_CREATE_USER)) {
                refTableVersionInfo.setCreateUser(rs.getString(RTVI_CREATE_USER));
            }
            if (isExistColumn(rs, RTVI_CREATE_TIME)) {
                refTableVersionInfo.setCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(RTVI_CREATE_TIME)));
            }
            if (isExistColumn(rs, RTVI_UPDATE_USER)) {
                refTableVersionInfo.setUpdateUser(rs.getString(RTVI_UPDATE_USER));
            }
            if (isExistColumn(rs, RTVI_UPDATE_TIME)) {
                refTableVersionInfo.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(RTVI_UPDATE_TIME)));
            }
            if (isExistColumn(rs, RTVI_DELETED)) {
                refTableVersionInfo.setDeleted(rs.getBoolean(RTVI_DELETED));
            }
            if (isExistColumn(rs, RTVI_DATASET_ID)) {
                refTableVersionInfo.setDatasetId(zeroToNull(rs, RTVI_DATASET_ID, Long.class));
            }

            return refTableVersionInfo;
        }
    }
}
