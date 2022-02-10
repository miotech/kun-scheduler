package com.miotech.kun.metadata.common.cataloger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CatalogerFilterTest {



    public static Stream<Arguments> filterTableParams() {
        return Stream.of(Arguments.of("database:table", true, true, true, true, true),
                Arguments.of("database:table", true, true, true, false, true),
                Arguments.of("database:table", true, true, false, true, true),
                Arguments.of("database:table", true, true, false, false, true),
                Arguments.of("database:table", true, false, true, true, true),
                Arguments.of("database:table", true, false, true, false, true),
                Arguments.of("database:table", true, false, false, true, true),
                Arguments.of("database:table", true, false, false, false, true),
                Arguments.of("database:table", false, true, true, true, false),
                Arguments.of("database:table", false, true, true, false, false),
                Arguments.of("database:table", false, true, false, true, true),
                Arguments.of("database:table", false, true, false, false, true),
                Arguments.of("database:table", false, false, true, true, false),
                Arguments.of("database:table", false, false, true, false, false),
                Arguments.of("database:table", false, false, false, true, false),
                Arguments.of("database:table", false, false, false, false, true));
    }

    public static Stream<Arguments> filterDatabaseParams() {
        return Stream.of(Arguments.of("database:table", true, true, true),
                Arguments.of("database:table", true, false, true),
                Arguments.of("database:table", false, true,  false),
                Arguments.of("database:table", false, false, true));
    }



    @ParameterizedTest
    @MethodSource("filterTableParams")
    public void filterTable(String tablePath, boolean dataBaseInWhiteList,boolean tableInWhiteList,
                            boolean databaseInBlackList, boolean tableInBlackList, boolean expect) {
        //prepare
        String[] databaseAndTable = tablePath.split(":");
        String database = databaseAndTable[0];
        String table = databaseAndTable[1];
        CatalogerBlackList blackList = new CatalogerBlackList();
        CatalogerWhiteList whiteList = new CatalogerWhiteList();
        if (dataBaseInWhiteList) {
            whiteList.addMember(database);
        }
        if (databaseInBlackList) {
            blackList.addMember(database);
        }
        if (tableInWhiteList) {
            whiteList.addMember(tablePath);
        }
        if (tableInBlackList) {
            blackList.addMember(tablePath);
        }
        CatalogerFilter filter = new CatalogerFilter(whiteList,blackList);

        boolean result = filter.filterTable(database,table);

        //verify
        assertThat(result,is(expect));

    }

    @ParameterizedTest
    @MethodSource("filterDatabaseParams")
    public void filterDatabase(String database, boolean dataBaseInWhiteList,boolean databaseInBlackList, boolean expect) {
        //prepare
        CatalogerBlackList blackList = new CatalogerBlackList();
        CatalogerWhiteList whiteList = new CatalogerWhiteList();
        if (dataBaseInWhiteList) {
            whiteList.addMember(database);
        }
        if (databaseInBlackList) {
            blackList.addMember(database);
        }
        CatalogerFilter filter = new CatalogerFilter(whiteList,blackList);

        boolean result = filter.filterDatabase(database);

        //verify
        assertThat(result,is(expect));

    }




}
