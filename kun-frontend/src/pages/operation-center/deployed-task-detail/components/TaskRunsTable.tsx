import React, { FunctionComponent, useCallback, useMemo } from 'react';
import { Table } from 'antd';
import moment from 'moment-timezone';
import momentDurationFormatSetup from 'moment-duration-format';
import { ColumnProps } from 'antd/es/table';
import { TaskRun } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';
import getLatestAttempt from '@/utils/getLatestAttempt';
import { StatusText } from '@/components/StatusText';

import styles from './TaskRunsTable.less';

interface TaskRunsTableProps {
  tableData?: TaskRun[];
  pageNum?: number;
  pageSize?: number;
  total?: number;
  onChangePagination?: (nextPageNum: number, pageSize?: number) => void;
  selectedTaskRun?: TaskRun | null;
  setSelectedTaskRun?: (taskRun: TaskRun | null) => any;
}

// @ts-ignore
momentDurationFormatSetup(moment);

const TaskRunsTable: FunctionComponent<TaskRunsTableProps> = (props) => {
  const t = useI18n();

  const {
    tableData = [],
    pageNum = 1,
    pageSize = 25,
    total = 0,
    onChangePagination,
    selectedTaskRun,
    setSelectedTaskRun,
  } = props;

  const columns: ColumnProps<TaskRun>[] = useMemo(() => [
    {
      dataIndex: 'id',
      title: 'Instance ID',
      key: 'id',
    },
    {
      key: 'status',
      title: t('taskRun.property.status'),
      render: (txt: any, record: TaskRun) => {
        const latestAttempt = getLatestAttempt(record);
        if (!latestAttempt) {
          return '-';
        }
        // else
        return <StatusText status={latestAttempt.status} />;
      },
    },
    {
      dataIndex: 'startAt',
      key: 'startAt',
      title: t('taskRun.property.startAt'),
      render: (txt: any, record: TaskRun) => {
        const latestAttempt = getLatestAttempt(record);
        if (!latestAttempt) {
          return '-';
        }
        // else
        const m = moment(latestAttempt.startAt);
        return m.isValid() ? m.format('YYYY-MM-DD HH:mm:ss') : '-';
      },
    },
    {
      dataIndex: 'endAt',
      key: 'endAt',
      title: t('taskRun.property.endAt'),
      render: (txt: any, record: TaskRun) => {
        const latestAttempt = getLatestAttempt(record);
        if (!latestAttempt) {
          return '-';
        }
        // else
        const m = moment(latestAttempt.endAt);
        return m.isValid() ? m.format('YYYY-MM-DD HH:mm:ss') : '-';
      },
    },
    {
      key: 'duration',
      title: t('taskRun.property.duration'),
      render: (txt: any, record: TaskRun) => {
        const latestAttempt = getLatestAttempt(record);
        if (!latestAttempt) {
          return '-';
        }
        // else
        const startMoment = moment(latestAttempt.startAt);
        const endMoment = moment(latestAttempt.endAt);
        if (startMoment.isValid() && endMoment.isValid()) {
          return moment.duration(endMoment.diff(startMoment)).format('h:mm:ss', {
            trim: false,
          });
        }
          return '-';

      },
    },
  ], []);

  const handleRowEvents = useCallback((record: TaskRun) => {
    return {
      onClick: () => {
        if (setSelectedTaskRun) {
          setSelectedTaskRun(record);
        }
      },
    };
  }, [
    setSelectedTaskRun,
  ]);

  return (
    <Table
      className={styles.TaskRunsTable}
      columns={columns}
      dataSource={tableData}
      rowKey="id"
      size="small"
      pagination={{
        current: pageNum,
        pageSize,
        total,
        onChange: onChangePagination,
      }}
      rowSelection={{
        selectedRowKeys: selectedTaskRun ? [selectedTaskRun.id] : [],
        type: 'radio',
        onSelect: (record) => {
          if (setSelectedTaskRun) {
            setSelectedTaskRun(record);
          }
        },
      }}
      onRow={handleRowEvents}
    />
  );
};

export default TaskRunsTable;
