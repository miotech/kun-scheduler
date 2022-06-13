package com.miotech.kun.metadata.databuilder.extract.statistics;

import com.miotech.kun.metadata.common.cataloger.Cataloger;
import com.miotech.kun.metadata.core.model.connection.ConnectionType;
import com.miotech.kun.metadata.core.model.constant.StatisticsMode;
import com.miotech.kun.metadata.core.model.dataset.Dataset;
import com.miotech.kun.metadata.core.model.dataset.FieldStatistics;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.databuilder.extract.impl.hive.HiveStatisticsExtractor;
import com.miotech.kun.metadata.databuilder.extract.template.DataWarehouseStatTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class StatisticsExtractorTemplate implements DatasetStatisticsExtractor {

    protected final DataSource dataSource;
    protected final Dataset dataset;
    protected final Cataloger cataloger;
    private final DataWarehouseStatTemplate statTemplate;
    private static final Logger logger = LoggerFactory.getLogger(StatisticsExtractorTemplate.class);
    private static final Integer maxExecuteSecond = 300;

    public StatisticsExtractorTemplate(DataSource dataSource, Dataset dataset, Cataloger cataloger) {
        this.dataSource = dataSource;
        this.dataset = dataset;
        this.statTemplate = buildDataWarehouseStatTemplate(dataSource, dataset);
        this.cataloger = cataloger;
    }

    private DataWarehouseStatTemplate buildDataWarehouseStatTemplate(DataSource dataSource, Dataset dataset) {
        ConnectionType connectionType = dataSource.getConnectionConfig().getDataConnection().getConnectionType();
        switch (connectionType) {
            case HIVE_SERVER:
            case ATHENA:
                return new DataWarehouseStatTemplate(dataset.getDatabaseName(), null, dataset.getName(), dataSource);
            case POSTGRESQL:
                String dbName = dataset.getDatabaseName().split("\\.")[0];
                String schemaName = dataset.getDatabaseName().split("\\.")[1];
                return new DataWarehouseStatTemplate(dbName, schemaName,
                        dataset.getName(), dataSource);
            default:
                throw new IllegalStateException(connectionType + " warehouse is not support yet");

        }
    }

    public Dataset extract(StatisticsMode statisticsMode) {
        Dataset.Builder resultBuilder = Dataset.newBuilder().withGid(dataset.getGid());

        if (statisticsMode.equals(StatisticsMode.FIELD)) {
            CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
            executeStatistics(() -> resultBuilder.withFieldStatistics(extractFieldStatistics()), cyclicBarrier);
            executeStatistics(() -> resultBuilder.withTableStatistics(extractTableStatistics()), cyclicBarrier);
            try {
                cyclicBarrier.await(maxExecuteSecond, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("extract:{} error", dataset.getGid(), e);
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException | TimeoutException e) {
                logger.error("extract:{} error", dataset.getGid(), e);
                throw new IllegalStateException(String.format("extract:%s error", dataset.getGid()), e);
            }

        } else if (statisticsMode.equals(StatisticsMode.TABLE)) {
            resultBuilder.withTableStatistics(extractTableStatistics());
        } else {
            throw new IllegalArgumentException("Invalid statisticsMode: " + statisticsMode);
        }

        return resultBuilder.build();
    }

    private void executeStatistics(Supplier<Dataset.Builder> supplier, CyclicBarrier cyclicBarrier) {
        new Thread(() -> {
            supplier.get();
            try {
                cyclicBarrier.await(maxExecuteSecond, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("extract:{} error", dataset.getGid(), e);
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException | TimeoutException e) {
                logger.error("extract:{} error", dataset.getGid(), e);
                throw new IllegalStateException(String.format("extract:%s error", dataset.getGid()), e);
            }

        }).start();
    }

    @Override
    public Long getRowCount() {
        return statTemplate.getRowCount(dataSource.getConnectionConfig().getDataConnection().getConnectionType());
    }

    @Override
    public List<FieldStatistics> extractFieldStatistics() {
        return statTemplate.extractFieldStatistics(dataset.getFields());
    }

}
