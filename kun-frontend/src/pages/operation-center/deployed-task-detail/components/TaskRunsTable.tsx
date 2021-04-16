import React, { FunctionComponent, useCallback, useMemo } from 'react';
import { Button, Popconfirm, Space, Table } from 'antd';
import moment from 'moment-timezone';
import momentDurationFormatSetup from 'moment-duration-format';
import { ColumnProps } from 'antd/es/table';
import { TaskRun } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';
import getLatestAttempt from '@/utils/getLatestAttempt';
import { StatusText } from '@/components/StatusText';
import { PauseOutlined, PlayCircleOutlined } from '@ant-design/icons';
import { RunStatusEnum } from "@/definitions/StatEnums.type";

import styles from './TaskRunsTable.less';

interface TaskRunsTableProps {
  tableData?: TaskRun[];
  pageNum?: number;
  pageSize?: number;
  total?: number;
  onChangePagination?: (nextPageNum: number, pageSize?: number) => void;
  selectedTaskRun?: TaskRun | null;
  setSelectedTaskRun?: (taskRun: TaskRun | null) => any;
  onClickStopTaskRun?: (taskRun: TaskRun | null) => any;
  onClickRerunTaskRun?: (taskRun: TaskRun | null) => any;
}

// @ts-ignore
momentDurationFormatSetup(moment);

function taskRunAlreadyComplete(status: RunStatusEnum): boolean {
  return  status === 'SUCCESS' ||
    status === 'SKIPPED' ||
    status === 'FAILED' ||
    status === 'ABORTED' ||
    status === 'ABORTING';
}

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
    onClickRerunTaskRun = () => {},
    onClickStopTaskRun = () => {},
  } = props;

  const columns: ColumnProps<TaskRun>[] = useMemo(() => [
    {
      dataIndex: 'id',
      title: t('taskRun.property.id'),
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
    {
      title: '',
      key: 'operations',
      width: 180,
      render: (txt, taskRun) => {
        const stopDisabled = taskRunAlreadyComplete(taskRun.status);
        return (
          <span>
              <Space size="small">
                <Popconfirm
                  title={t('taskRun.abort.alert')}
                  disabled={stopDisabled}
                  onConfirm={(ev) => {
                    ev?.stopPropagation();
                    onClickStopTaskRun(taskRun);
                  }}
                >
                  <Button
                    icon={<PauseOutlined />}
                    size="small"
                    danger
                    disabled={stopDisabled}
                    onClick={(ev) => { ev.stopPropagation(); }}
                  >
                    {/* 中止 */}
                    {t('taskRun.abort')}
                  </Button>
                </Popconfirm>
                <Popconfirm
                  title={t('taskRun.rerun.alert')}
                  onConfirm={(ev) => {
                    ev?.stopPropagation();
                    onClickRerunTaskRun(taskRun);
                  }}
                >
                  <Button
                    icon={<PlayCircleOutlined />}
                    size="small"
                    onClick={(ev) => { ev.stopPropagation(); }}
                  >
                    {/* 重新运行 */}
                    {t('taskRun.rerun')}
                  </Button>
                </Popconfirm>
              </Space>
            </span>
        );
      },
    },
  ], [t]);

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
