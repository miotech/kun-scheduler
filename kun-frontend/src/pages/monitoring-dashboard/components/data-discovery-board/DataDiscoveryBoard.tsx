import React, { memo, useCallback, useMemo } from 'react';
import { Col, Row } from 'antd';
import { useUpdateEffect } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { RatioCard } from '@/components/Monitoring/RatioCard/RatioCard';
import { TopMaxRowCountChangeTable } from '@/pages/monitoring-dashboard/components/data-discovery-board/TopMaxRowCountChangeTable';
import { FailedTestCasesTable } from '@/pages/monitoring-dashboard/components/data-discovery-board/FailedTestCasesTable';
import { DatasetsMetricsTable } from '@/pages/monitoring-dashboard/components/data-discovery-board/DatasetsMetricsTable';
import { TableOnChangeCallback } from '@/definitions/common-types';
import { ColumnMetrics, FailedTestCase } from '@/services/monitoring-dashboard';
import normalizeSingleColumnSorter from '@/utils/normalizeSingleColumnSorter';

export const DataDiscoveryBoard: React.FC = memo(function DataDiscoveryBoard() {
  const t = useI18n();

  const { selector: {
    dataDiscoveryBoardData: {
      metadataMetrics,
      maxRowCountChange,
      failedTestCases,
      datasetMetrics,
      metadataMetricsLoading,
    },
  }, dispatch } = useRedux(state => ({
    dataDiscoveryBoardData: state.monitoringDashboard.dataDiscoveryBoardData,
  }));

  const {
    dataQualityCoveredCount,
    dataQualityLongExistingFailedCount,
    dataQualityPassCount,
    totalCaseCount,
    totalDatasetCount,
  } = metadataMetrics || {};

  // setup pagination change listener for failed test cases table
  useUpdateEffect(() => {
    dispatch.monitoringDashboard.reloadFailedTestCases({
      pageSize: failedTestCases.pageSize,
      pageNumber: failedTestCases.pageNum,
      sortColumn: failedTestCases.sortColumn || undefined,
      sortOrder: failedTestCases.sortOrder || undefined,
    });
  }, [
    failedTestCases.pageNum,
    failedTestCases.pageSize,
    failedTestCases.sortColumn,
    failedTestCases.sortOrder,
  ]);

  // setup pagination change listener for datasets table
  useUpdateEffect(() => {
    dispatch.monitoringDashboard.reloadDatasetMetrics({
      pageNumber: datasetMetrics.pageNum,
      pageSize: datasetMetrics.pageSize,
    });
  }, [
    datasetMetrics.pageNum,
    datasetMetrics.pageSize,
  ]);

  // table change handler for failed test cases table
  const handleFailedTestCasesTableChange: TableOnChangeCallback<FailedTestCase> =
    useCallback((pagination, filters, sorter) => {
      const {
        sortOrder,
        sortColumn,
      } = normalizeSingleColumnSorter(sorter);

      dispatch.monitoringDashboard.setFailedTestCases({
        ...failedTestCases,
        pageNum: pagination.current || failedTestCases.pageNum,
        pageSize: pagination.pageSize || failedTestCases.pageSize,
        sortOrder,
        sortColumn,
      });
    }, [
      dispatch.monitoringDashboard,
      failedTestCases,
    ]);

  // table change handler for datasets table
  const handleDatasetMetricsTableChange: TableOnChangeCallback<ColumnMetrics> =
    useCallback((pagination) => {
      dispatch.monitoringDashboard.setDatasetMetrics({
        ...datasetMetrics,
        pageNum: pagination.current || datasetMetrics.pageNum,
        pageSize: pagination.pageSize || datasetMetrics.pageSize,
      });
    }, [
      dispatch.monitoringDashboard,
      datasetMetrics,
    ]);

  const ratioCardSection = useMemo(() => {
    return (
      <Row gutter={[8, 8]}>
        {/* Covered Ratio */}
        <Col span={8}>
          <RatioCard
            title={t('monitoringDashboard.dataDiscovery.coveredRatio')}
            numerator={dataQualityCoveredCount}
            denominator={totalDatasetCount}
            status="healthy"
            loading={metadataMetricsLoading}
          />
        </Col>
        {/* Count of Long-existing Failed Case */}
        <Col span={8}>
          <RatioCard
            title={t('monitoringDashboard.dataDiscovery.longExistingFailedCaseCount')}
            numerator={dataQualityLongExistingFailedCount}
            denominator={totalCaseCount}
            status="warning"
            loading={metadataMetricsLoading}
          />
        </Col>
        {/* Pass Ratio (Last 24 hours) */}
        <Col span={8}>
          <RatioCard
            title={t('monitoringDashboard.dataDiscovery.passRatio')}
            numerator={dataQualityPassCount}
            denominator={totalCaseCount}
            status="healthy"
            loading={metadataMetricsLoading}
          />
        </Col>
      </Row>
    );
  }, [
    t,
    dataQualityCoveredCount,
    totalDatasetCount,
    dataQualityLongExistingFailedCount,
    totalCaseCount,
    dataQualityPassCount,
    metadataMetricsLoading,
  ]);

  return (
    <div id="data-discovery-board">
      {/* Ratio cards row section */}
      {ratioCardSection}
      {/* Top 10 Datasets with max row count change table */}
      <Row gutter={[8, 8]}>
        <Col span={24}>
          <TopMaxRowCountChangeTable
            data={maxRowCountChange.data}
            loading={maxRowCountChange.loading}
          />
        </Col>
      </Row>
      {/* Failed Test cases table */}
      <Row gutter={[8, 8]}>
        <Col span={24}>
          <FailedTestCasesTable
            data={failedTestCases.data}
            pageNum={failedTestCases.pageNum}
            pageSize={failedTestCases.pageSize}
            total={failedTestCases.total}
            onChange={handleFailedTestCasesTableChange}
            loading={failedTestCases.loading}
          />
        </Col>
      </Row>

      {/* Dataset metrics table */}
      <DatasetsMetricsTable
        data={datasetMetrics.data}
        pageNum={datasetMetrics.pageNum}
        pageSize={datasetMetrics.pageSize}
        total={datasetMetrics.total}
        onChange={handleDatasetMetricsTableChange}
        loading={datasetMetrics.loading}
      />
    </div>
  );
});
