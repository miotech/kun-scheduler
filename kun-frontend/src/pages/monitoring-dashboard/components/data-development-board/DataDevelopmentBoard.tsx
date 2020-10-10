import React, { memo, useCallback, useMemo } from 'react';
import { Col, Row } from 'antd';
import { StatisticCard } from '@/components/Monitoring/StatisticCard/StatisticCard';
import useI18n from '@/hooks/useI18n';
import { DailyTaskFinishCountChart } from '@/pages/monitoring-dashboard/components/data-development-board/DailyTaskFinishCountChart';
import { TaskDetailsTable } from '@/pages/monitoring-dashboard/components/data-development-board/TaskDetailsTable';
import useRedux from '@/hooks/useRedux';
import { TableOnChangeCallback } from '@/definitions/common-types';
import { DevTaskDetail } from '@/services/monitoring-dashboard';
import { useUpdateEffect } from 'ahooks';

export const DataDevelopmentBoard: React.FC = memo(function DataDevelopmentBoard() {
  const t = useI18n();

  const {
    selector: {
      dataDevelopmentBoardData,
    },
    dispatch,
  } = useRedux(s => ({
    dataDevelopmentBoardData: s.monitoringDashboard.dataDevelopmentBoardData,
  }));

  const {
    dataDevelopmentMetrics: metrics,
    dailyTaskFinish,
    taskDetails,
    dataDevelopmentMetricsLoading,
  } = dataDevelopmentBoardData;

  /* Reload table after pagination change */
  useUpdateEffect(() => {
    dispatch.monitoringDashboard.reloadTaskDetails({
      pageNumber: taskDetails.pageNum,
      pageSize: taskDetails.pageSize,
    });
  }, [
    taskDetails.pageNum,
    taskDetails.pageSize,
  ]);

  const topMetricsRow = useMemo(() => {
    return (
      <Row gutter={[8, 8]}>
        <Col span={6}>
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.successLastDay')}
            value={metrics.successTaskCount}
            textTheme="success"
            loading={dataDevelopmentMetricsLoading}
          />
        </Col>
        <Col span={6}>
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.failedLastDay')}
            value={metrics.failedTaskCount}
            textTheme="failed"
            loading={dataDevelopmentMetricsLoading}
          />
        </Col>
        <Col span={6}>
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.running')}
            value={metrics.runningTaskCount}
            textTheme="running"
            loading={dataDevelopmentMetricsLoading}
          />
        </Col>
        <Col span={6}>
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.totalTaskCount')}
            value={metrics.totalTaskCount}
            textTheme="default"
            loading={dataDevelopmentMetricsLoading}
          />
        </Col>
      </Row>
    );
  }, [
    t,
    metrics.successTaskCount,
    metrics.failedTaskCount,
    metrics.runningTaskCount,
    metrics.totalTaskCount,
    dataDevelopmentMetricsLoading,
  ]);

  const taskDetailsTableChangeHandler: TableOnChangeCallback<DevTaskDetail> = useCallback((pagination) => {
    dispatch.monitoringDashboard.updateTaskDetails({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    });
  }, [
    dispatch,
  ]);

  return (
    <div id="data-development-board">
      {/* Top metric cards */}
      {topMetricsRow}
      {/* Daily task finish count chart */}
      <Row gutter={[8, 8]}>
        <Col span={24}>
          <DailyTaskFinishCountChart
            data={dailyTaskFinish.data}
          />
        </Col>
      </Row>
      {/* Task details table */}
      <Row gutter={[8, 8]}>
        <Col span={24}>
          <TaskDetailsTable
            loading={taskDetails.loading}
            pageNum={taskDetails.pageNum}
            pageSize={taskDetails.pageSize}
            total={taskDetails.total}
            data={taskDetails.data}
            onChange={taskDetailsTableChangeHandler}
          />
        </Col>
      </Row>
    </div>
  );
});
