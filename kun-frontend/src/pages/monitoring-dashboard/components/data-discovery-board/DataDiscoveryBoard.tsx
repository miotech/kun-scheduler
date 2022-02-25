import React, { memo, useCallback, useMemo } from 'react';
import { Col, Row } from 'antd';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { RatioCard } from '@/components/Monitoring/RatioCard/RatioCard';
import { FailedTestCasesTable } from '@/pages/monitoring-dashboard/components/data-discovery-board/FailedTestCasesTable';
import { TableOnChangeCallback } from '@/definitions/common-types';
import { AbnormalDataset } from '@/services/monitoring-dashboard';
import normalizeSingleColumnSorter from '@/utils/normalizeSingleColumnSorter';

export const DataDiscoveryBoard: React.FC = memo(function DataDiscoveryBoard() {
  const t = useI18n();

  const {
    selector: {
      dataDiscoveryBoardData: { metadataMetrics, failedTestCases, metadataMetricsLoading },
    },
    dispatch,
  } = useRedux(state => ({
    dataDiscoveryBoardData: state.monitoringDashboard.dataDiscoveryBoardData,
  }));

  const {
    dataQualityCoveredCount,
    dataQualityLongExistingFailedCount,
    dataQualityPassCount,
    totalCaseCount,
    totalDatasetCount,
  } = metadataMetrics || {};

  // table change handler for failed test cases table
  const handleFailedTestCasesTableChange: TableOnChangeCallback<AbnormalDataset> = useCallback(
    (pagination, filters, sorter) => {
      const { sortOrder, sortColumn } = normalizeSingleColumnSorter(sorter);

      dispatch.monitoringDashboard.setFailedTestCases({
        ...failedTestCases,
        pageNum: pagination.current || failedTestCases.pageNum,
        pageSize: pagination.pageSize || failedTestCases.pageSize,
        sortOrder,
        sortColumn,
      });
    },
    [dispatch.monitoringDashboard, failedTestCases],
  );

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
      {/* Failed Test cases table */}
      <Row gutter={[8, 8]}>
        <Col span={24}>
          <FailedTestCasesTable
            glossaryFilter={failedTestCases.glossaryFilter}
            data={failedTestCases.data}
            pageNum={failedTestCases.pageNum}
            pageSize={failedTestCases.showPageSize}
            total={failedTestCases.total}
            onChange={handleFailedTestCasesTableChange}
            loading={failedTestCases.loading}
          />
        </Col>
      </Row>
    </div>
  );
});
