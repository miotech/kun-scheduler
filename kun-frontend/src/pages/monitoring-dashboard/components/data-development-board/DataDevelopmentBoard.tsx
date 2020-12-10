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
import { DataDevelopmentBoardFilterCardType } from '@/rematch/models/monitoringDashboard/model-state';

function computeFilterTypeToRequestParam(selectedFilterCardType: DataDevelopmentBoardFilterCardType): string | undefined {
  switch (selectedFilterCardType) {
    case 'SUCCESS':
      return 'SUCCESS';
    case 'FAILED':
      return 'FAILED,ERROR,ABORTED';
    case 'PENDING':
      return 'QUEUED,CREATED';
    case 'RUNNING':
      return 'RUNNING';
    default:
      break;
  }
  return undefined;
}

export const DataDevelopmentBoard: React.FC = memo(function DataDevelopmentBoard() {
  const t = useI18n();

  const {
    selector: {
      selectedFilterCardType,
      dataDevelopmentBoardData,
    },
    dispatch,
  } = useRedux(s => ({
    selectedFilterCardType: s.monitoringDashboard.dataDevelopmentBoardData.taskDetailsSelectedFilter,
    dataDevelopmentBoardData: s.monitoringDashboard.dataDevelopmentBoardData,
  }));

  const {
    dataDevelopmentMetrics: metrics,
    dailyTaskFinish,
    taskDetails,
    dataDevelopmentMetricsLoading,
  } = dataDevelopmentBoardData;

  const setSelectedFilterCardType = useCallback((payload: DataDevelopmentBoardFilterCardType) => {
    dispatch.monitoringDashboard.setTaskDetailsSelectedFilter(payload);
    dispatch.monitoringDashboard.setTaskDetails({
      ...taskDetails,
      pageNum: 1,
    });
  }, [
    dispatch,
    taskDetails,
  ]);

  /* Reload table after pagination change */
  useUpdateEffect(() => {
    dispatch.monitoringDashboard.reloadTaskDetails({
      pageNumber: taskDetails.pageNum,
      pageSize: taskDetails.pageSize,
      taskRunStatus: (selectedFilterCardType != null) ? computeFilterTypeToRequestParam(selectedFilterCardType) : undefined,
    });
  }, [
    taskDetails.pageNum,
    taskDetails.pageSize,
    selectedFilterCardType,
  ]);

  const topMetricsRow = useMemo(() => {
    return (
      <Row gutter={[8, 8]}>
        <Col flex="1 1">
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.successLastDay')}
            value={metrics.successTaskCount}
            textTheme="success"
            loading={dataDevelopmentMetricsLoading}
            selectedAsFilter={selectedFilterCardType === 'SUCCESS'}
            onClick={() => {
              if (selectedFilterCardType !== 'SUCCESS') {
                setSelectedFilterCardType('SUCCESS');
              } else {
                setSelectedFilterCardType(null);
              }
            }}
          />
        </Col>
        <Col flex="1 1">
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.failedLastDay')}
            value={metrics.failedTaskCount}
            textTheme="failed"
            loading={dataDevelopmentMetricsLoading}
            selectedAsFilter={selectedFilterCardType === 'FAILED'}
            onClick={() => {
              if (selectedFilterCardType !== 'FAILED') {
                setSelectedFilterCardType('FAILED');
              } else {
                setSelectedFilterCardType(null);
              }
            }}
          />
        </Col>
        <Col flex="1 1">
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.running')}
            value={metrics.runningTaskCount}
            textTheme="running"
            loading={dataDevelopmentMetricsLoading}
            selectedAsFilter={selectedFilterCardType === 'RUNNING'}
            onClick={() => {
              if (selectedFilterCardType !== 'RUNNING') {
                setSelectedFilterCardType('RUNNING');
              } else {
                setSelectedFilterCardType(null);
              }
            }}
          />
        </Col>
        <Col flex="1 1">
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.pending')}
            value={metrics.pendingTaskCount}
            textTheme="pending"
            loading={dataDevelopmentMetricsLoading}
            selectedAsFilter={selectedFilterCardType === 'PENDING'}
            onClick={() => {
              if (selectedFilterCardType !== 'PENDING') {
                setSelectedFilterCardType('PENDING');
              } else {
                setSelectedFilterCardType(null);
              }
            }}
          />
        </Col>
        <Col flex="1 1">
          <StatisticCard
            title={t('monitoringDashboard.dataDevelopment.totalTaskCount')}
            value={metrics.totalTaskCount}
            textTheme="default"
            loading={dataDevelopmentMetricsLoading}
            onClick={() => {
              setSelectedFilterCardType(null);
            }}
          />
        </Col>
      </Row>
    );
  }, [
    t,
    metrics.successTaskCount,
    metrics.failedTaskCount,
    metrics.runningTaskCount,
    metrics.pendingTaskCount,
    metrics.totalTaskCount,
    dataDevelopmentMetricsLoading,
    selectedFilterCardType,
    setSelectedFilterCardType,
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
            loading={dailyTaskFinish.loading}
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
