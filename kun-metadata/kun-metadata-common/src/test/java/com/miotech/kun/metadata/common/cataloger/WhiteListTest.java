package com.miotech.kun.metadata.common.cataloger;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WhiteListTest {

    @Test
    public void testMerge(){
        Set<String> whiteSet1 = Sets.newHashSet("database1","database2:table1",
                "database3.schema1:table1","database3.schema2:table1","database3.schema1:table2");
        Set<String> whiteSet2 = Sets.newHashSet("database2","database2:table1",
                "database3.schema1:table1","database3.schema2:table2");
        CatalogerWhiteList whiteList1 = new CatalogerWhiteList(whiteSet1);
        CatalogerWhiteList whiteList2 = new CatalogerWhiteList(whiteSet2);
        CatalogerWhiteList whiteList3 = whiteList1.merge(whiteList2);

        assertThat(whiteList3.size(),is(7));
        assertTrue(whiteList3.contains("database1"));
        assertTrue(whiteList3.contains("database2:table1"));
        assertTrue(whiteList3.contains("database3.schema1:table1"));
        assertTrue(whiteList3.contains("database3.schema2:table1"));
        assertTrue(whiteList3.contains("database3.schema1:table2"));
        assertTrue(whiteList3.contains("database2"));
        assertTrue(whiteList3.contains("database3.schema2:table2"));
    }

    @Test
    public void keyContainsInWhiteList_should_return_ture(){
        //prepare
        CatalogerWhiteList whiteList = new CatalogerWhiteList();

        whiteList.addMember("newMember");

        //verify
        assertTrue(whiteList.contains("newMember"));

    }

    @Test
    public void keyNotContainsInWhiteList_should_return_false(){
        //prepare
        Set<String> whiteSet = Sets.newHashSet("database2","database2:table1",
                "database3.schema1:table1","database3.schema2:table2");
        CatalogerWhiteList whiteList = new CatalogerWhiteList(whiteSet);

        whiteList.removeMember("database2");

        //verify
        assertFalse(whiteList.contains("database2"));
    }
}
