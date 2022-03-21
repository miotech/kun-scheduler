package com.miotech.kun.metadata.common.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.ResultSetMapper;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.utils.JSONUtils;
import com.miotech.kun.metadata.common.utils.SearchOptionJoiner;
import com.miotech.kun.metadata.core.model.constant.ResourceType;
import com.miotech.kun.metadata.core.model.constant.SearchContent;
import com.miotech.kun.metadata.core.model.search.ResourceAttribute;
import com.miotech.kun.metadata.core.model.search.SearchFilterOption;
import com.miotech.kun.metadata.core.model.search.SearchedInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.shaded.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

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
    //    private static final String COLUMN_RANK_FORMAT = "ts_rank_cd(search_ts , to_tsquery(?)) AS rank";
    private static final String COLUMN_SELECT = new StringJoiner(",")
            .add(COLUMN_GID).add(COLUMN_RESOURCE_TYPE).add(COLUMN_NAME).add(COLUMN_DESCRIPTION)
            .add(COLUMN_RESOURCE_ATTRIBUTE).add(COLUMN_DELETED).toString();
    private static final String[] COLUMNS = new String[]{COLUMN_GID, COLUMN_RESOURCE_TYPE, COLUMN_NAME, COLUMN_DESCRIPTION,
            COLUMN_RESOURCE_ATTRIBUTE, COLUMN_UPDATE_TIME, COLUMN_DELETED};


    public List<SearchedInfo> search(List<SearchFilterOption> searchFilterOptionList, Set<String> resourceTypeSet, Integer pageNumber, Integer pageSize) {
        String optionsString = new SearchOptionJoiner().add(searchFilterOptionList).toString();
        logger.debug("search options:optionsString:{},resource Type{},limit page{},num{}", optionsString, resourceTypeSet, pageNumber, pageSize);
        String sql = DefaultSQLBuilder.newBuilder().select(COLUMN_SELECT, similarity(searchFilterOptionList))
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(COLUMN_SEARCH_TS + " @@ to_tsquery(?) and  resource_type in " + collectionToConditionSql(resourceTypeSet) + " and deleted =false ").orderBy("rank desc").getSQL();
        String limitSql = toLimitSql(pageNumber, pageSize);
        String resultSql = sql.concat(limitSql);
        List<Object> params = Lists.newArrayList();
        params.add(optionsString);
        params.addAll(resourceTypeSet);
        return dbOperator.fetchAll(resultSql, UniversalSearchMapper.INSTANCE, params.toArray());
    }

    private String similarity(List<SearchFilterOption> searchFilterOptionList) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        searchFilterOptionList.forEach(searchFilterOption -> stringJoiner.add(SearchOptionJoiner.escapeSql(searchFilterOption.getKeyword())));
        StringJoiner sql = new StringJoiner("+");
        String keyword = stringJoiner.toString();
        String sqlString = sql.add(String.format(" similarity (%s,'%s')*" + SearchContent.NAME.getWeightNum(), COLUMN_NAME, keyword))
                .add(String.format("similarity (%s ,'%s')*" + SearchContent.DESCRIPTION.getWeightNum(), COLUMN_DESCRIPTION, keyword))
                .add(String.format(" similarity (coalesce((select  string_agg(distinct(value), ',') from jsonb_each_text(%s)),''),'%s')*" +
                        SearchContent.ATTRIBUTE.getWeightNum(), COLUMN_RESOURCE_ATTRIBUTE, keyword))
                .toString();
        return sqlString.concat(" as rank");
    }

    public Integer searchCount(List<SearchFilterOption> searchFilterOptionList, Set<String> resourceTypeSet) {
        String optionsString = new SearchOptionJoiner().add(searchFilterOptionList).toString();
        logger.debug("search count options:optionsString:{},resource Type{}", optionsString, resourceTypeSet);
        String sql = DefaultSQLBuilder.newBuilder().select("count(1) as count")
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(COLUMN_SEARCH_TS + " @@ to_tsquery(?) and  resource_type in " + collectionToConditionSql(resourceTypeSet) + " and deleted =false ").getSQL();
        List<Object> params = Lists.newArrayList();
        params.add(optionsString);
        params.addAll(resourceTypeSet);
        return dbOperator.fetchOne(sql, rs -> rs.getInt("count"), params.toArray());
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
        dbOperator.update(sql, params.toArray());

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

    public String collectionToConditionSql(Collection<?> collection) {
        if (CollectionUtils.isNotEmpty(collection)) {
            StringBuilder collectionSql = new StringBuilder("(");
            for (Object object : collection) {
                collectionSql.append("?").append(",");
            }
            collectionSql.deleteCharAt(collectionSql.length() - 1);
            collectionSql.append(")");
            return collectionSql.toString();
        }
        return "";
    }

    public String toLimitSql(int pageNum, int pageSize) {
        StringJoiner pageSql = new StringJoiner(" ", " ", "");
        pageSql.add("limit ").add(String.valueOf(pageSize)).add(" offset ").add(String.valueOf((pageNum - 1) * pageSize));
        return pageSql.toString();
    }
}
