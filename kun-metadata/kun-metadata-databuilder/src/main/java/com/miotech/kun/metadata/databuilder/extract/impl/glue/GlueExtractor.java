package com.miotech.kun.metadata.databuilder.extract.impl.glue;

import com.amazonaws.services.glue.model.Table;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.databuilder.extract.Extractor;
import com.miotech.kun.metadata.databuilder.extract.iterator.GlueTableIterator;
import com.miotech.kun.metadata.databuilder.model.AWSDataSource;
import com.miotech.kun.metadata.databuilder.model.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlueExtractor implements Extractor {
    private static final Logger logger = LoggerFactory.getLogger(GlueExtractor.class);

    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

    private final AWSDataSource dataSource;

    public GlueExtractor(AWSDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Iterator<Dataset> extract() {
        GlueTableIterator glueTableIterator = new GlueTableIterator(dataSource.getGlueAccessKey(),
                dataSource.getGlueSecretKey(), dataSource.getGlueRegion());

        List<Table> tables = Lists.newArrayList();
        List<Dataset> datasets = Lists.newCopyOnWriteArrayList();
        while (glueTableIterator.hasNext()) {
            Table table = glueTableIterator.next();
            tables.add(table);
        }

        CountDownLatch countDownLatch = new CountDownLatch(tables.size());
        for (Table table : tables) {
            threadPool.submit(() -> {
                try {
                    Dataset dataset = new GlueTableExtractor(dataSource, table).extract().next();
                    datasets.add(dataset);
                } catch (Exception e) {
                    logger.error("AWSTableExtractor extract error:", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("AWSExtractor countDownLatch.await error:", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
        return datasets.iterator();
    }

}
