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
import com.miotech.kun.metadata.core.model.vo.UniversalSearchRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
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
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String TS_CONFIG = "english";

    //    private static final String COLUMN_RANK_FORMAT = "ts_rank_cd(search_ts , to_tsquery(?)) AS rank";
    private static final String COLUMN_SELECT = new StringJoiner(",")
            .add(COLUMN_GID).add(COLUMN_RESOURCE_TYPE).add(COLUMN_NAME).add(COLUMN_DESCRIPTION)
            .add(COLUMN_RESOURCE_ATTRIBUTE)
            .add(COLUMN_UPDATE_TIME)
            .add(COLUMN_DELETED)
            .add(COLUMN_CREATE_TIME)
            .toString();
    private static final String[] COLUMNS = new String[]{COLUMN_GID, COLUMN_RESOURCE_TYPE, COLUMN_NAME, COLUMN_DESCRIPTION,
            COLUMN_RESOURCE_ATTRIBUTE, COLUMN_UPDATE_TIME, COLUMN_DELETED, COLUMN_CREATE_TIME};


    public List<SearchedInfo> search(UniversalSearchRequest request) {
        List<SearchFilterOption> searchFilterOptionList = request.getSearchFilterOptions();
        Set<String> resourceTypeSet = request.getResourceTypeNames();
        Integer pageNumber = request.getPageNumber();
        Integer pageSize = request.getPageSize();
        Map<String, Object> resourceAttributeMap = request.getResourceAttributeMap();
        boolean showDeleted = request.isShowDeleted();
        OffsetDateTime startCreateTime = request.getStartCreateTime();
        OffsetDateTime endCreateTime = request.getEndCreateTime();
        OffsetDateTime startUpdateTime = request.getStartUpdateTime();
        OffsetDateTime endUpdateTime = request.getEndUpdateTime();
        String optionsString = new SearchOptionJoiner().add(searchFilterOptionList).toString();
        logger.debug("search options:optionsString:{},resource Type{},limit page{},num{}", optionsString, resourceTypeSet, pageNumber, pageSize);
        if (optionsString.isEmpty()) {
            return Lists.newArrayList();
        }
        List<Object> params = Lists.newArrayList();
        params.add(optionsString);
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMN_SELECT, similarity(searchFilterOptionList))
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(COLUMN_SEARCH_TS + " @@ to_tsquery('" + TS_CONFIG + "',?) "
                        + filterType(params, resourceTypeSet)
                        + filterResourceAttributes(resourceAttributeMap)
                        + filterTime(params, COLUMN_CREATE_TIME, startCreateTime, endCreateTime)
                        + filterTime(params, COLUMN_UPDATE_TIME, startUpdateTime, endUpdateTime)
                        + showDeleted(showDeleted))
                .orderBy("rank desc").getSQL();
        String limitSql = toLimitSql(pageNumber, pageSize);
        String resultSql = sql.concat(limitSql);
        return dbOperator.fetchAll(resultSql, UniversalSearchMapper.INSTANCE, params.toArray());
    }

    private String filterType(List<Object> params, Collection<String> resourceTypeSet) {
        if (CollectionUtils.isEmpty(resourceTypeSet)) {
            return StringUtils.EMPTY;
        }
        return " and " + COLUMN_RESOURCE_TYPE + " in " + collectionToConditionSql(params, resourceTypeSet);
    }


    public List<SearchedInfo> noneKeywordPage(UniversalSearchRequest request) {
        Set<String> resourceTypeSet = request.getResourceTypeNames();
        Integer pageNumber = request.getPageNumber();
        Integer pageSize = request.getPageSize();
        Map<String, Object> resourceAttributeMap = request.getResourceAttributeMap();
        boolean showDeleted = request.isShowDeleted();
        OffsetDateTime startCreateTime = request.getStartCreateTime();
        OffsetDateTime endCreateTime = request.getEndCreateTime();
        OffsetDateTime startUpdateTime = request.getStartUpdateTime();
        OffsetDateTime endUpdateTime = request.getEndUpdateTime();
        logger.debug("search options:resourceTypeSet:{},limit page{},num{}", resourceTypeSet, pageNumber, pageSize);
        List<Object> params = Lists.newArrayList();
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMN_SELECT)
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(" 1=1 "
                        + filterType(params, resourceTypeSet)
                        + filterResourceAttributes(resourceAttributeMap)
                        + showDeleted(showDeleted)
                        + filterTime(params, COLUMN_CREATE_TIME, startCreateTime, endCreateTime)
                        + filterTime(params, COLUMN_UPDATE_TIME, startUpdateTime, endUpdateTime)
                )
                .orderBy(" update_time desc")
                .getSQL();
        String limitSql = toLimitSql(pageNumber, pageSize);
        String resultSql = sql.concat(limitSql);
        return dbOperator.fetchAll(resultSql, UniversalSearchMapper.INSTANCE, params.toArray());
    }


    public Integer searchCount(UniversalSearchRequest request) {
        List<SearchFilterOption> searchFilterOptionList = request.getSearchFilterOptions();
        Set<String> resourceTypeSet = request.getResourceTypeNames();
        Map<String, Object> resourceAttributeMap = request.getResourceAttributeMap();
        boolean showDeleted = request.isShowDeleted();
        OffsetDateTime startCreateTime = request.getStartCreateTime();
        OffsetDateTime endCreateTime = request.getEndCreateTime();
        OffsetDateTime startUpdateTime = request.getStartUpdateTime();
        OffsetDateTime endUpdateTime = request.getEndUpdateTime();
        String optionsString = new SearchOptionJoiner().add(searchFilterOptionList).toString();
        logger.debug("search count options:optionsString:{},resource Type{}", optionsString, resourceTypeSet);
        List<Object> params = Lists.newArrayList();
        params.add(optionsString);
        String sql = DefaultSQLBuilder.newBuilder().select("count(1) as count")
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(COLUMN_SEARCH_TS + " @@ to_tsquery('" + TS_CONFIG + "',?)  "
                        + filterType(params, resourceTypeSet)
                        + filterResourceAttributes(resourceAttributeMap)
                        + filterTime(params, COLUMN_CREATE_TIME, startCreateTime, endCreateTime)
                        + filterTime(params, COLUMN_UPDATE_TIME, startUpdateTime, endUpdateTime)
                        + showDeleted(showDeleted)).getSQL();

        return dbOperator.fetchOne(sql, rs -> rs.getInt("count"), params.toArray());
    }

    public Integer noneKeywordSearchCount(UniversalSearchRequest request) {
        Set<String> resourceTypeSet = request.getResourceTypeNames();
        Map<String, Object> resourceAttributeMap = request.getResourceAttributeMap();
        boolean showDeleted = request.isShowDeleted();
        OffsetDateTime startCreateTime = request.getStartCreateTime();
        OffsetDateTime endCreateTime = request.getEndCreateTime();
        OffsetDateTime startUpdateTime = request.getStartUpdateTime();
        OffsetDateTime endUpdateTime = request.getEndUpdateTime();
        logger.debug("search count resource Type{}", resourceTypeSet);
        List<Object> params = Lists.newArrayList();
        String sql = DefaultSQLBuilder.newBuilder().select("count(1) as count")
                .from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH)
                .where(" 1=1 "
                        + filterType(params, resourceTypeSet)
                        + filterResourceAttributes(resourceAttributeMap)
                        + filterTime(params, COLUMN_CREATE_TIME, startCreateTime, endCreateTime)
                        + filterTime(params, COLUMN_UPDATE_TIME, startUpdateTime, endUpdateTime)
                        + showDeleted(showDeleted)).getSQL();
        return dbOperator.fetchOne(sql, rs -> rs.getInt("count"), params.toArray());
    }

    public void update(SearchedInfo searchedInfo) {
        ImmutableList<String> setOptions = ImmutableList.of(COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_RESOURCE_ATTRIBUTE, COLUMN_UPDATE_TIME);
        ImmutableList<String> options = ImmutableList.of(COLUMN_GID, COLUMN_RESOURCE_TYPE);
        OffsetDateTime now = DateTimeUtils.now();
        OffsetDateTime updateTime = Objects.isNull(searchedInfo.getUpdateTime()) ? now : searchedInfo.getUpdateTime();
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                StringUtils.stripToEmpty(searchedInfo.getName()),
                StringUtils.stripToEmpty(searchedInfo.getDescription()),
                JSONUtils.toJsonString(searchedInfo.getResourceAttribute()),
                updateTime,
                searchedInfo.getGid(),
                searchedInfo.getResourceType().name());

        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_KUN_MT_UNIVERSAL_SEARCH)
                .set(set(setOptions))
                .where(and(options)).getSQL();
        dbOperator.update(sql, params.toArray());
    }

    public void save(SearchedInfo searchedInfo) {
        OffsetDateTime now = DateTimeUtils.now();
        OffsetDateTime createTime = Objects.isNull(searchedInfo.getCreateTime()) ? now : searchedInfo.getCreateTime();
        OffsetDateTime updateTime = Objects.isNull(searchedInfo.getUpdateTime()) ? now : searchedInfo.getUpdateTime();
        ImmutableList<? extends Serializable> params = ImmutableList.of(
                searchedInfo.getGid(),
                searchedInfo.getResourceType().name(),
                StringUtils.stripToEmpty(searchedInfo.getName()),
                StringUtils.stripToEmpty(searchedInfo.getDescription()),
                JSONUtils.toJsonString(searchedInfo.getResourceAttribute()),
                updateTime,
                false,
                createTime
        );
        String sql = DefaultSQLBuilder.newBuilder().insert(COLUMNS).into(TABLE_KUN_MT_UNIVERSAL_SEARCH).asPrepared().getSQL();
        dbOperator.update(sql, params.toArray());

    }

    public SearchedInfo find(ResourceType resourceType, Long gid) {
        ImmutableList<String> options = ImmutableList.of(COLUMN_GID, COLUMN_RESOURCE_TYPE);
        ImmutableList<? extends Serializable> params = ImmutableList.of(gid, resourceType.name());
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

    public List<String> fetchResourceAttributeList(ResourceType resourceType, String resourceAttributeName, Map<String, Object> resourceAttributeMap, Boolean showDeleted) {
        logger.debug("fetch Resource Attribute List resource Type{}", resourceType);
        String sql = DefaultSQLBuilder.newBuilder().select("distinct  resource_attribute->>? as attribute").from(TABLE_KUN_MT_UNIVERSAL_SEARCH, TABLE_A_KUN_MT_UNIVERSAL_SEARCH).where(" resource_type = ?"
                + filterResourceAttributes(resourceAttributeMap)
                + showDeleted(showDeleted)).orderBy(" attribute ").getSQL();
        List<Object> params = Lists.newArrayList();
        params.add(resourceAttributeName);
        params.add(resourceType.name());
        List<String> list = dbOperator.fetchAll(sql, rs -> rs.getString("attribute"), params.toArray());
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }


    private static class UniversalSearchMapper implements ResultSetMapper<SearchedInfo> {
        public static final ResultSetMapper<SearchedInfo> INSTANCE = new UniversalSearchDao.UniversalSearchMapper();

        @Override
        public SearchedInfo map(ResultSet rs) throws SQLException {
            return SearchedInfo.Builder.newBuilder()
                    .withGid(rs.getLong(COLUMN_GID))
                    .withResourceType(ResourceType.valueOf(rs.getString(COLUMN_RESOURCE_TYPE)))
                    .withName(rs.getString(COLUMN_NAME))
                    .withDescription(rs.getString(COLUMN_DESCRIPTION))
                    .withResourceAttribute(parseResourceAttribute(
                            ResourceType.valueOf(rs.getString(COLUMN_RESOURCE_TYPE)), rs.getString(COLUMN_RESOURCE_ATTRIBUTE)))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(COLUMN_CREATE_TIME)))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp(COLUMN_UPDATE_TIME)))
                    .withDeleted(rs.getBoolean(COLUMN_DELETED))
                    .build();

        }

        public ResourceAttribute parseResourceAttribute(ResourceType resourceType, String resourceAttributeJson) {
            return JSONUtils.jsonToObject(resourceAttributeJson, resourceType.getResourceAttributeClass());
        }
    }

    private String similarity(List<SearchFilterOption> searchFilterOptionList) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        searchFilterOptionList.forEach(searchFilterOption -> stringJoiner.add(SearchOptionJoiner.escapeSql(searchFilterOption.getKeyword())));
        StringJoiner sql = new StringJoiner("+");
        String keyword = stringJoiner.toString();
        String sqlString = sql.add(String.format(" similarity (coalesce(%s,''),'%s')*" + SearchContent.NAME.getWeightNum(), COLUMN_NAME, keyword))
                .add(String.format("similarity (coalesce(%s,'') ,'%s')*" + SearchContent.DESCRIPTION.getWeightNum(), COLUMN_DESCRIPTION, keyword))
                .add(String.format(" similarity (coalesce((select  string_agg(distinct(value), ',') from jsonb_each_text(%s)),''),'%s')*" +
                        SearchContent.ATTRIBUTE.getWeightNum(), COLUMN_RESOURCE_ATTRIBUTE, keyword))
                .toString();
        return sqlString.concat(" as rank");
    }

    private static String and(ImmutableList<String> list) {
        StringJoiner and = new StringJoiner(" AND ");
        list.forEach((s -> and.add(String.format("%s=?", s))));
        return and.toString();
    }

    private static String set(ImmutableList<String> list) {
        StringJoiner and = new StringJoiner(",");
        list.forEach((s -> and.add(String.format("%s=?", s))));
        return and.toString();
    }

    private String collectionToConditionSql(List<Object> params, Collection<?> collection) {
        if (CollectionUtils.isNotEmpty(collection)) {
            params.addAll(collection);
            StringBuilder collectionSql = new StringBuilder("(");
            for (Object ignored : collection) {
                collectionSql.append("?").append(",");
            }
            collectionSql.deleteCharAt(collectionSql.length() - 1);
            collectionSql.append(")");
            return collectionSql.toString();
        }
        return "";
    }

    private String toLimitSql(int pageNum, int pageSize) {
        StringJoiner pageSql = new StringJoiner(" ", " ", "");
        pageSql.add("limit ").add(String.valueOf(pageSize)).add(" offset ").add(String.valueOf((pageNum - 1) * pageSize));
        return pageSql.toString();
    }

    private String filterTime(List<Object> params, String colName, OffsetDateTime startTime, OffsetDateTime endTime) {
        if (Objects.isNull(startTime) && Objects.isNull(endTime)) {
            return StringUtils.EMPTY;
        }
        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
            if (startTime.compareTo(endTime) > 0) {
                return StringUtils.EMPTY;
            }
            params.add(startTime);
            params.add(endTime);
            return String.format(" and %s between ? and ?", colName);
        }
        if (Objects.isNull(startTime)) {
            params.add(endTime);
            return String.format(" and %s <= ? ", colName);
        }
        params.add(startTime);
        return String.format(" and %s >= ? ", colName);

    }

    private String filterResourceAttributes(Map<String, Object> resourceAttributeMap) {
        if (MapUtils.isEmpty(resourceAttributeMap)) {
            return "";
        }
        String jsonString = JSONUtils.toJsonString(resourceAttributeMap);
        return " and " + COLUMN_RESOURCE_ATTRIBUTE + " @>'" + jsonString + "'";
    }

    private String showDeleted(boolean showDeleted) {
        if (showDeleted) {
            return "";
        }
        return " and deleted =false ";
    }

}
