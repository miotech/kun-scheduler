package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.core.model.dataset.TableStatistics;
import com.miotech.kun.metadata.databuilder.extract.fileparser.ParquetParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParquetParserTest {

    @Test
    public void testParse() {
        ParquetParser parquetParser = new ParquetParser();
        TableStatistics statistics = parquetParser.parse("file:///" + ParquetParserTest.class.getResource("/parquet").getPath(), "", "");
        assertThat(statistics.getRowCount(), is(20L));
        assertThat(statistics.getTotalByteSize(), is(258L));
    }

}
