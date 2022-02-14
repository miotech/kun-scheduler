package com.miotech.kun.metadata.common.cataloger;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlackListTest {

    @Test
    public void testMerge(){
        Set<String> blackSet1 = Sets.newHashSet("database1","database2:table1",
                "database3.schema1:table1","database3.schema2:table1","database3.schema1:table2");
        Set<String> blackSet2 = Sets.newHashSet("database2","database2:table1",
                "database3.schema1:table1","database3.schema2:table2");
        CatalogerBlackList blackList1 = new CatalogerBlackList(blackSet1);
        CatalogerBlackList blackList2 = new CatalogerBlackList(blackSet2);
        CatalogerBlackList blackList3 = blackList1.merge(blackList2);

        assertThat(blackList3.size(),is(7));
        assertTrue(blackList3.contains("database1"));
        assertTrue(blackList3.contains("database2:table1"));
        assertTrue(blackList3.contains("database3.schema1:table1"));
        assertTrue(blackList3.contains("database3.schema2:table1"));
        assertTrue(blackList3.contains("database3.schema1:table2"));
        assertTrue(blackList3.contains("database2"));
        assertTrue(blackList3.contains("database3.schema2:table2"));
    }

    @Test
    public void keyContainsInBlackList_should_return_ture(){
        //prepare
        CatalogerBlackList blackList = new CatalogerBlackList();

        blackList.addMember("newMember");

        //verify
        assertTrue(blackList.contains("newMember"));

    }

    @Test
    public void keyNotContainsInBlackList_should_return_false(){
        //prepare
        Set<String> blackSet = Sets.newHashSet("database2","database2:table1",
                "database3.schema1:table1","database3.schema2:table2");
        CatalogerBlackList blackList = new CatalogerBlackList(blackSet);

        blackList.removeMember("database2");

        //verify
        assertFalse(blackList.contains("database2"));
    }
}
