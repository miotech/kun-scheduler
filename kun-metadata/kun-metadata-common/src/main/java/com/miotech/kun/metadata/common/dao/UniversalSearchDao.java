package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.service.SearchService;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.search.ResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import org.apache.curator.shaded.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @program: kun
 * @description: search Dao
 * @author: zemin  huang
 * @create: 2022-03-08 10:16
 **/
@Singleton
public class UniversalSearchDao {
    @Inject
    private DatabaseOperator dbOperator;
    private final Logger logger = LoggerFactory.getLogger(UniversalSearchDao.class);
    private static final String TABLE_KUN_MT_UNIVERSAL_SEARCH = "kun_mt_universal_search";
    private static final String TABLE_A_KUN_MT_UNIVERSAL_SEARCH = "kmus";
    private static final String COLUMN_GID = "gid";
    private static final String COLUMN_RESOURCE_TYPE = "resource_type";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_RESOURCE_ATTRIBUTE = "resource_attribute";
    private static final String COLUMN_SEARCH_TS = "search_ts";
    private static final String COLUMN_UPDATE_TIME = "update_time";
    private static final String COLUMN_DELETED = "deleted";
    private static final String COLUMN_RANK_FORMAT = "ts_rank_cd(search_ts , '%s') AS rank";
    private static final String COLUMN_SELECT = new StringJoiner(",")
            .add(COLUMN_GID).add(COLUMN_RESOURCE_TYPE).add(COLUMN_NAME).add(COLUMN_DESCRIPTION)
            .add(COLUMN_RESOURCE_ATTRIBUTE).add(COLUMN_DELETED).toString();
    private static final String[] COLUMNS = new String[]{COLUMN_GID,COLUMN_RESOURCE_TYPE,COLUMN_NAME,COLUMN_DESCRIPTION,
            COLUMN_RESOURCE_ATTRIBUTE,COLUMN_UPDATE_TIME,COLUMN_DELETED};


    public List<SearchedInfo> search(String optionString,Integer limitNum) {

        String sql = DefaultSQLBuilder.newBuilder().select(COLUMN_SELECT, createRankColumn(optionString))
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(COLUMN_SEARCH_TS + " @@ to_tsquery(?)").orderBy("rank desc").limit(limitNum).getSQL();
        List<SearchedInfo> searchedInfos = dbOperator.fetchAll(sql, UniversalSearchMapper.INSTANCE, optionString);
       return searchedInfos.stream().filter(searchedInfo ->!searchedInfo.isDeleted()).collect(Collectors.toList());
    }

    public static String createRankColumn(String optionString) {
        return String.format(COLUMN_RANK_FORMAT, optionString);
    }

    public void update(SearchedInfo searchedInfo) {
        ImmutableList<String> setOptions = ImmutableList.of(COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_RESOURCE_ATTRIBUTE, COLUMN_UPDATE_TIME);
        ImmutableList<String> options = ImmutableList.of(COLUMN_GID, COLUMN_RESOURCE_TYPE);

        ImmutableList<? extends Serializable> params = ImmutableList.of(
                searchedInfo.getName(),
                searchedInfo.getDescription(),
                JSONUtils.toJsonString(searchedInfo.getResourceAttribute()),
                DateTimeUtils.now(),
                searchedInfo.getGid(),
                searchedInfo.getResourceType().name());

        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KUN_MT_UNIVERSAL_SEARCH)
                .set(set(setOptions))
                .where(and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
    }

    public void save(SearchedInfo searchedInfo) {

        ImmutableList<? extends Serializable> params = ImmutableList.of(
                searchedInfo.getGid(),
                searchedInfo.getResourceType().name(),
                searchedInfo.getName(),
                searchedInfo.getDescription(),
                JSONUtils.toJsonString(searchedInfo.getResourceAttribute()),
                DateTimeUtils.now(),
                false
        );
        String sql = DefaultSQLBuilder.newBuilder().insert(COLUMNS).into(TABLE_KUN_MT_UNIVERSAL_SEARCH).asPrepared().getSQL();
        dbOperator.update(sql,  params.toArray());

    }

    public SearchedInfo find(ResourceType resourceType, Long gid) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_GID, COLUMN_RESOURCE_TYPE, COLUMN_DELETED);
        ImmutableList<? extends Serializable> params = ImmutableList.of(gid, resourceType.name(), false);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMNS)
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(and(options)).getSQL();
        return dbOperator.fetchOne(sql, UniversalSearchMapper.INSTANCE, params.toArray());
    }


    public void remove(ResourceType resourceType, Long gid) {
        ImmutableList<String> setOptions = ImmutableList.of(COLUMN_UPDATE_TIME, COLUMN_DELETED);
        ImmutableList<String> options = ImmutableList.of(COLUMN_GID, COLUMN_RESOURCE_TYPE);
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                DateTimeUtils.now(),
                true,
                gid,
                resourceType.name()
        );
        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KUN_MT_UNIVERSAL_SEARCH)
                .set(set(setOptions))
                .where(and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
    }


    private static class UniversalSearchMapper implements ResultSetMapper<SearchedInfo> {
        public static final ResultSetMapper<SearchedInfo> INSTANCE = new UniversalSearchDao.UniversalSearchMapper();
        private final Logger logger = LoggerFactory.getLogger(UniversalSearchMapper.class);
        @Override
        public SearchedInfo map(ResultSet rs) throws SQLException {
            return SearchedInfo.Builder.newBuilder()
                    .withGid(rs.getLong(COLUMN_GID))
                    .withResourceType(ResourceType.valueOf(rs.getString(COLUMN_RESOURCE_TYPE)))
                    .withDescription(COLUMN_RESOURCE_TYPE)
                    .withName(rs.getString(COLUMN_NAME))
                    .withDescription(rs.getString(COLUMN_DESCRIPTION))
                    .withResourceAttribute(parseResourceAttribute(
                            ResourceType.valueOf(rs.getString(COLUMN_RESOURCE_TYPE)), rs.getString(COLUMN_RESOURCE_ATTRIBUTE)))
                    .withDeleted(rs.getBoolean(COLUMN_DELETED))
                    .build();

        }

        public ResourceAttribute parseResourceAttribute(ResourceType resourceType, String resourceAttributeJson) {
            return JSONUtils.jsonToObject(resourceAttributeJson, resourceType.getResourceAttributeClass());
        }
    }

    public static String and(ImmutableList<String> list) {
        StringJoiner and = new StringJoiner(" AND ");
        list.forEach((s -> and.add(String.format("%s=?", s))));
        return and.toString();
    }

    public static String set(ImmutableList<String> list) {
        StringJoiner and = new StringJoiner(",");
        list.forEach((s -> and.add(String.format("%s=?", s))));
        return and.toString();
    }
}
