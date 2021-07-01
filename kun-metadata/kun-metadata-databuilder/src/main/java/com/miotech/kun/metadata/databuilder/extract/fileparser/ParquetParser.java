package com.miotech.kun.metadata.databuilder.extract.fileparser;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.core.model.dataset.TableStatistics;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.parquet.hadoop.Footer;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class ParquetParser {

    public TableStatistics parse(String location, Configuration configuration) {
        Path inputPath = new Path(location);
        try {
            FileSystem fileSystem = inputPath.getFileSystem(configuration);
            LocalDateTime lastUpdatedTime = null;

            RemoteIterator<LocatedFileStatus> locatedFileStatusRemoteIterator = fileSystem.listFiles(inputPath, true);
            long rowCount = 0;
            long totalByteSize = 0;
            while (locatedFileStatusRemoteIterator.hasNext()) {
                LocatedFileStatus locatedFileStatus = locatedFileStatusRemoteIterator.next();

                LocalDateTime fileModificationTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(locatedFileStatus.getModificationTime()), TimeZone.getDefault().toZoneId());
                if (lastUpdatedTime == null) {
                    lastUpdatedTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(locatedFileStatus.getModificationTime()), TimeZone.getDefault().toZoneId());
                } else {
                    lastUpdatedTime = lastUpdatedTime.isBefore(fileModificationTime) ? fileModificationTime: lastUpdatedTime;
                }

                if (!locatedFileStatus.getPath().getName().endsWith(".parquet")) {
                    continue;
                }
                for (Footer f : ParquetFileReader.readFooters(configuration, locatedFileStatus, false)) {
                    for (BlockMetaData b : f.getParquetMetadata().getBlocks()) {
                        rowCount += b.getRowCount();
                        totalByteSize += b.getTotalByteSize();
                    }
                }
            }

            return TableStatistics.newBuilder()
                    .withRowCount(rowCount)
                    .withTotalByteSize(totalByteSize)
                    .withLastUpdatedTime(lastUpdatedTime)
                    .withStatDate(LocalDateTime.now())
                    .build();
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public TableStatistics parse(String location, String accessKey, String secretKey) {
        Configuration conf = new Configuration();
        conf.set("fs.s3a.access.key", accessKey);
        conf.set("fs.s3a.secret.key", secretKey);

        return parse(location, conf);
    }

}
