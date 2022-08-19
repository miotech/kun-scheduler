package com.miotech.kun.datadiscovery.service.rdm.file;

import com.miotech.kun.datadiscovery.model.entity.RefTableVersionInfo;
import com.miotech.kun.datadiscovery.model.entity.rdm.*;
import com.miotech.kun.datadiscovery.model.enums.ColumnType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.api.InitContext;
import org.apache.parquet.hadoop.api.ReadSupport;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.api.RecordMaterializer;
import org.apache.parquet.schema.MessageType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final RefStorageParquetFileResolver refFileResolver;
    private final Configuration filSystemConfiguration;

    public boolean overwrite(StorageFileData storageFileData) throws IOException {
        LinkedHashSet<RefColumn> columns = storageFileData.getRefBaseTable().getRefTableMetaData().getColumns();
        MessageType messageType = ParquetUtils.createMessageType(storageFileData.getSchema(), columns);
        List<RefColumn> sortColumns = columns.stream().sorted(Comparator.comparing(RefColumn::getIndex)).collect(Collectors.toList());
        ParquetWriter<Group> writer = getGroupParquetWriter(storageFileData, messageType);
        SimpleGroupFactory simpleGroupFactory = new SimpleGroupFactory(messageType);
        for (DataRecord item : storageFileData.getRefBaseTable().getRefData().getData()) {
            Group group = simpleGroupFactory.newGroup();
            sortColumns.forEach(refColumn -> ParquetUtils.addGroupItem(ColumnType.columnType(refColumn.getColumnType()), group, refColumn.getName(), item.get(refColumn.getName())));
            writer.write(group);
        }
        writer.close();
        return true;
    }

    private ParquetWriter<Group> getGroupParquetWriter(StorageFileData storageFileData, MessageType messageType) throws IOException {
        return ExampleParquetWriter.builder(new Path(storageFileData.getDataPath()))
                .withConf(filSystemConfiguration)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .withWriterVersion(ParquetProperties.WriterVersion.PARQUET_1_0)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withType(messageType)
                .build();
    }

    public StorageFileData read(RefTableVersionInfo refTableVersionInfo) throws IOException {
        ParquetReader<Group> reader = getGroupParquetReader(refTableVersionInfo);
        RefData refData = refFileResolver.resolve(reader, refTableVersionInfo.getRefTableColumns());
        RefBaseTable refBaseTable = new RefBaseTable(new RefTableMetaData(refTableVersionInfo.getRefTableColumns(), refTableVersionInfo.getRefTableConstraints()), refData);
        return new StorageFileData(refTableVersionInfo.getDataPath(), refTableVersionInfo.getTableName(), refBaseTable);
    }

    private ParquetReader<Group> getGroupParquetReader(RefTableVersionInfo refTableVersionInfo) throws IOException {
        ReadSupport<Group> mapReadSupport = new ReadSupport<Group>() {
            @Override
            public ReadContext init(InitContext context) {
                return new ReadContext(ParquetUtils.createMessageType(refTableVersionInfo.getSchemaName(), refTableVersionInfo.getRefTableColumns()));
            }

            @Override
            public RecordMaterializer<Group> prepareForRead(Configuration configuration, Map keyValueMetaData, MessageType fileSchema, ReadContext readContext) {
                return new GroupRecordConverter(fileSchema);
            }
        };
        ParquetReader.Builder<Group> builder = ParquetReader.builder(mapReadSupport, new Path(refTableVersionInfo.getDataPath()));
        return builder.withConf(filSystemConfiguration).build();
    }
}
