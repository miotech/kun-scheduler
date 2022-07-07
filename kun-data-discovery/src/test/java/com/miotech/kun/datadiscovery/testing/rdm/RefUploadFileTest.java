package com.miotech.kun.datadiscovery.testing.rdm;


import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.service.rdm.file.RefUploadFileBuilder;
import com.miotech.kun.datadiscovery.service.rdm.file.UploadFileType;
import com.miotech.kun.datadiscovery.testing.DataDiscoveryTestBase;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import static com.miotech.kun.datadiscovery.testing.mockdata.MockRefDataVersionBasicFactory.getMultipartCSVFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:59
 **/
public class RefUploadFileTest extends DataDiscoveryTestBase {
    @Autowired
    private RefUploadFileBuilder refUploadFileBuilder;


    @SneakyThrows
    @Test
    public void parseCsv() {
        String fileName = "test.csv";
        MultipartFile mulFile = getMultipartCSVFile(fileName, "/test.csv");
        RefInputSource refInputSource = new RefInputSource(mulFile);
        RefUploadFileDescription refUploadFileDescription = refInputSource.getRefUploadFileDescription();
        assertThat(refUploadFileDescription.getUploadFileType(), is(UploadFileType.CSV));
        assertThat(refUploadFileDescription.getFileName(), is(fileName));
        RefBaseTable table = refUploadFileBuilder.parse(refInputSource);
        RefData refData = table.getRefData();
        assertThat(refData, is(notNullValue()));
        assertThat(refData.getData(), is(notNullValue()));
        assertThat(refData.getData().size(), is(10));
        RefTableMetaData refTableMetaData = table.getRefTableMetaData();
        assertThat(refTableMetaData, is(notNullValue()));
        assertThat(refTableMetaData.getColumns().size(), is(4));

    }

    @SneakyThrows
    @Test
    public void parseExecl() {
        String fileName = "test.xlsx";
        MultipartFile mulFile = getMultipartCSVFile(fileName, "/test.xlsx");
        RefInputSource refInputSource = new RefInputSource(mulFile);
        RefUploadFileDescription refUploadFileDescription = refInputSource.getRefUploadFileDescription();
        assertThat(refUploadFileDescription.getUploadFileType(), is(UploadFileType.EXECL));
        assertThat(refUploadFileDescription.getFileName(), is(fileName));
        RefBaseTable table = refUploadFileBuilder.parse(refInputSource);
        RefData refData = table.getRefData();
        assertThat(refData, is(notNullValue()));
        assertThat(refData.getData(), is(notNullValue()));
        assertThat(refData.getData().size(), is(10));
        RefTableMetaData refTableMetaData = table.getRefTableMetaData();
        assertThat(refTableMetaData, is(notNullValue()));
        assertThat(refTableMetaData.getColumns().size(), is(4));
    }
}
