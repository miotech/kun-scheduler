import React, { FunctionComponent, useCallback, useMemo, useState } from 'react';
import { Button, Popconfirm, Select, Space, Table, Tooltip, Popover, Dropdown, Menu } from 'antd';
import range from 'lodash/range';
import moment from 'moment-timezone';
import momentDurationFormatSetup from 'moment-duration-format';
import { ColumnProps } from 'antd/es/table';
import { TaskRun } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';
import getLatestAttempt from '@/utils/getLatestAttempt';
import { StatusText } from '@/components/StatusText';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import Icon, {
  StepForwardOutlined,
  CaretDownOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { ReactComponent as StopIcon } from '@/assets/icons/stop.svg';
import { ReactComponent as RerunIcon } from '@/assets/icons/rerun.svg';
import { dayjs } from '@/utils/datetime-utils';
import { TaskAttempt } from '@/definitions/TaskAttempt.type';
import { skipTaskrun } from '@/services/task-deployments/deployed-tasks';
import { DependenceRemove } from './DependenceRemove';
import TaskRerunModal from './TaskRerunModal';
import styles from './TaskRunsTable.less';
import UpstreamFailed from './UpstreamFailed';

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
  selectedAttemptMap: Record<string, number>;
  setSelectedAttemptMap: (nextState: Record<string, number>) => any;
  refreshTaskRun: () => any;
}

// @ts-ignore
momentDurationFormatSetup(moment);

function taskRunAlreadyComplete(status: RunStatusEnum): boolean {
  return (
    status === 'SUCCESS' ||
    status === 'SKIPPED' ||
    status === 'FAILED' ||
    status === 'ABORTED' ||
    status === 'ABORTING' ||
    status === 'CHECK_FAILED'
  );
}

function convertScheduledTick(tickValue?: string) {
  if (!tickValue) {
    return '-';
  }
  // else
  return dayjs(
    `${tickValue.slice(0, 4)}-${tickValue.slice(4, 6)}-${tickValue.slice(6, 8)} ${tickValue.slice(
      8,
      10,
    )}:${tickValue.slice(10, 12)}`,
  ).format('YYYY-MM-DD HH:mm:ss');
}

const TaskRunsTable: FunctionComponent<TaskRunsTableProps> = props => {
  const t = useI18n();
  const [visible, setVisible] = useState<boolean>(false);
  const [currentId, setCurrentId] = useState<string>('');
  const [isModalVisible, setIsModalVisible] = useState<boolean>(false);

  const [popConfirmVisible, setPopConfirmVisible] = useState(false);
  const [popConfirmLoading, setPopConfirmLoading] = useState(false);

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
    selectedAttemptMap,
    setSelectedAttemptMap,
    refreshTaskRun = () => {},
  } = props;

  const onClickSkipTaskRun = useCallback(
    async (taskRun: TaskRun) => {
      const res = await skipTaskrun(taskRun.id);
      if (res) {
        refreshTaskRun();
      }
    },
    [refreshTaskRun],
  );

  const DropdownMenu = useCallback(
    (taskRun: TaskRun) => {
      const stopDisabled = taskRunAlreadyComplete(taskRun.status);

      return (
        <Menu>
          {stopDisabled && (
            <Menu.Item key="rerun">
              <Popconfirm
                title={t('taskRun.rerun.alert')}
                visible={popConfirmVisible}
                okButtonProps={{ loading: popConfirmLoading }}
                onCancel={() => setPopConfirmVisible(false)}
                onConfirm={async () => {
                  setPopConfirmLoading(true);
                  await onClickRerunTaskRun(taskRun);
                  setPopConfirmLoading(false);
                  setPopConfirmVisible(false);
                }}
              >
                <Button
                  icon={<Icon component={RerunIcon} />}
                  size="small"
                  type="text"
                  onClick={() => {
                    setPopConfirmVisible(true);
                  }}
                >
                  {/* 重新运行 */}
                  {t('taskRun.rerun')}
                </Button>
              </Popconfirm>
            </Menu.Item>
          )}
          {!stopDisabled && (
            <Menu.Item key="stopTask">
              <Space size="small">
                <Popconfirm
                  title={t('taskRun.abort.alert')}
                  disabled={stopDisabled}
                  onConfirm={ev => {
                    ev?.stopPropagation();
                    onClickStopTaskRun(taskRun);
                  }}
                >
                  <Button
                    icon={<Icon component={StopIcon} />}
                    size="small"
                    type="text"
                    onClick={ev => {
                      ev.stopPropagation();
                    }}
                  >
                    {/* 中止 */}
                    {t('taskRun.abort')}
                  </Button>
                </Popconfirm>
              </Space>
            </Menu.Item>
          )}
          {stopDisabled && (
            <Menu.Item key="rerunDownstream">
              <Button
                icon={<SyncOutlined />}
                size="small"
                type="text"
                onClick={() => {
                  setIsModalVisible(true);
                  setCurrentId(taskRun.id);
                }}
              >
                {/* 重跑及下游任务 */}
                {t('taskRun.rerunDownstream.button')}
              </Button>
            </Menu.Item>
          )}
          {!stopDisabled && (
            <Menu.Item key="dependence">
              <Button
                icon={<CloseCircleOutlined />}
                size="small"
                type="text"
                onClick={() => {
                  setVisible(true);
                  setCurrentId(taskRun.id);
                }}
              >
                {/* 解除依赖 */}
                {t('taskRun.dependence')}
              </Button>
            </Menu.Item>
          )}
          {['FAILED', 'ABORTED', 'CHECK_FAILED'].includes(taskRun.status) && (
            <Menu.Item key="skip">
              <Popconfirm
                title={t('taskRun.skip.alert')}
                onConfirm={ev => {
                  ev?.stopPropagation();
                  onClickSkipTaskRun(taskRun);
                }}
              >
                <Button icon={<CheckCircleOutlined />} size="small" type="text">
                  {/* 任务跳过 */}
                  {t('taskRun.skip')}
                </Button>
              </Popconfirm>
            </Menu.Item>
          )}
        </Menu>
      );
    },
    [
      onClickStopTaskRun,
      setVisible,
      setCurrentId,
      onClickRerunTaskRun,
      onClickSkipTaskRun,
      popConfirmVisible,
      popConfirmLoading,
      setPopConfirmVisible,
      setPopConfirmLoading,
      t,
    ],
  );

  const getCorrespondingAttempt = useCallback(
    (record: TaskRun) => {
      if (!selectedAttemptMap[record.id]) {
        return getLatestAttempt(record);
      }
      // else
      return record.attempts.find(i => i.attempt === selectedAttemptMap[record.id]) as TaskAttempt;
    },
    [selectedAttemptMap],
  );

  const columns: ColumnProps<TaskRun>[] = useMemo(
    () => [
      {
        dataIndex: 'id',
        title: t('taskRun.property.id'),
        key: 'id',
      },
      {
        key: 'attempt',
        title: t('taskRun.property.attempt'),
        width: 86,
        render: (txt: unknown, record: TaskRun) => {
          const maxAttempt = Math.max(...record.attempts.map(i => i.attempt));
          const options = range(1, maxAttempt + 1).map(attempt => (
            <Select.Option value={attempt} key={`${record.id}-attempt-${attempt}`}>
              {attempt}
            </Select.Option>
          ));
          return (
            <span>
              <Select
                style={{ width: '45px' }}
                size="small"
                value={getCorrespondingAttempt(record)?.attempt || maxAttempt}
                onChange={nextAttempt => {
                  if (Number(nextAttempt)) {
                    setSelectedAttemptMap({
                      ...selectedAttemptMap,
                      [record.id]: Number(nextAttempt),
                    });
                  }
                }}
              >
                {options}
              </Select>
              {(getCorrespondingAttempt(record)?.attempt || maxAttempt) < maxAttempt ? (
                <Tooltip title={t('taskRun.property.attempt.jumpToLatest')}>
                  <Button
                    type="link"
                    size="small"
                    icon={<StepForwardOutlined />}
                    onClick={() => {
                      // @ts-ignore
                      setSelectedAttemptMap({
                        ...selectedAttemptMap,
                        [record.id]: null,
                      });
                    }}
                  />
                </Tooltip>
              ) : (
                <></>
              )}
            </span>
          );
        },
      },
      {
        key: 'status',
        title: t('taskRun.property.status'),
        render: (txt: any, record: TaskRun) => {
          const latestAttempt = getCorrespondingAttempt(record);
          if (!latestAttempt) {
            return '-';
          }
          // else
          if (latestAttempt.status === 'UPSTREAM_FAILED') {
            return (
              <Popover
                title={t('taskRun.property.failedLink')}
                content={<UpstreamFailed failedUpstreamTaskRuns={record.failedUpstreamTaskRuns} />}
              >
                <div>
                  <StatusText status={latestAttempt.status} />
                </div>
              </Popover>
            );
          }
          return <StatusText status={latestAttempt.status} />;
        },
      },
      {
        dataIndex: 'createdAt',
        key: 'startAt',
        title: t('taskRun.property.scheduledAt'),
        render: (txt: any, record: TaskRun) => {
          const latestAttempt = getLatestAttempt(record);
          if (!latestAttempt) {
            return '-';
          }
          // else
          return convertScheduledTick(record.scheduledTick?.time);
        },
      },
      {
        dataIndex: 'startAt',
        key: 'startAt',
        title: t('taskRun.property.startAt'),
        render: (txt: any, record: TaskRun) => {
          const latestAttempt = getCorrespondingAttempt(record);
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
          const latestAttempt = getCorrespondingAttempt(record);
          if (!latestAttempt) {
            return '-';
          }
          // else
          const startMoment = moment(latestAttempt.startAt);
          const endMoment = moment(latestAttempt.endAt);
          return endMoment.isValid() &&
            (!startMoment.isValid() || endMoment.toDate().getTime() - startMoment.toDate().getTime() >= 0)
            ? endMoment.format('YYYY-MM-DD HH:mm:ss')
            : '-';
        },
      },
      {
        key: 'duration',
        title: t('taskRun.property.duration'),
        render: (txt: any, record: TaskRun) => {
          const latestAttempt = getCorrespondingAttempt(record);
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
        width: 140,
        align: 'center',
        render: (txt, taskRun) => {
          return (
            <Dropdown overlay={() => DropdownMenu(taskRun)}>
              <a onClick={e => e.preventDefault()}>
                <Space>
                  {t('common.column.action')}
                  <CaretDownOutlined />
                </Space>
              </a>
            </Dropdown>
          );
        },
      },
    ],
    [getCorrespondingAttempt, selectedAttemptMap, DropdownMenu, setSelectedAttemptMap, t],
  );

  const handleRowEvents = useCallback(
    (record: TaskRun) => {
      return {
        onClick: () => {
          if (setSelectedTaskRun) {
            setSelectedTaskRun(record);
          }
        },
      };
    },
    [setSelectedTaskRun],
  );

  return (
    <>
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
          columnWidth: 30,
          onSelect: record => {
            if (setSelectedTaskRun) {
              setSelectedTaskRun(record);
            }
          },
        }}
        onRow={handleRowEvents}
      />
      {visible && (
        <DependenceRemove
          refreshTaskRun={refreshTaskRun}
          visible={visible}
          currentId={currentId}
          onCancel={() => setVisible(false)}
        />
      )}
      {isModalVisible && (
        <TaskRerunModal
          refreshTaskRun={refreshTaskRun}
          taskRunId={currentId}
          isModalVisible={isModalVisible}
          setIsModalVisible={setIsModalVisible}
        />
      )}
    </>
  );
};

export default TaskRunsTable;
