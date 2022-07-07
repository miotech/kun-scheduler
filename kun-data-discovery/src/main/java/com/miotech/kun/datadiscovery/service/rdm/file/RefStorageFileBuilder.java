package com.miotech.kun.datadiscovery.service.rdm.file;

import com.miotech.kun.datadiscovery.model.entity.rdm.RefData;
import com.miotech.kun.datadiscovery.model.entity.rdm.StorageFileData;
import com.miotech.kun.datadiscovery.service.rdm.StorageFileManger;
import com.miotech.kun.datadiscovery.util.FormatParserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Map;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-06-23 17:59
 **/
@RequiredArgsConstructor
@Component
@Slf4j
public class RefStorageFileBuilder {
    private final RefStorageCsvFileResolver refFileResolver;
    private final StorageFileManger fileManger;

    @NotNull
    public StorageFileData read(String name) throws IOException {
        InputStream is = fileManger.getObjectContent(name);
        RefData refData = refFileResolver.resolve(is);
        return new StorageFileData(name, refData);

    }


    public boolean override(StorageFileData storageFileData) throws IOException {
        InputStream inputStream = createStorageFile(storageFileData.getRefData());
        fileManger.putObject(storageFileData.getDataPath(), inputStream);
        inputStream.close();
        return true;
    }

    private InputStream createStorageFile(RefData refData) throws IOException {
        CSVFormat csvFormat = FormatParserUtils.defaultCSVFormat();
        ByteArrayOutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;
        ByteArrayInputStream byteArrayInputStream;
        try {
            outputStream = new ByteArrayOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            CSVPrinter printer = new CSVPrinter(bufferedWriter, csvFormat);
            printer.printRecord(refData.getHeaderMap().entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toArray());
            printer.printRecords(refData.getData());
            bufferedWriter.flush();
            byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
        } finally {
            bufferedWriter.close();
            outputStream.close();
        }
        return byteArrayInputStream;
    }

}
