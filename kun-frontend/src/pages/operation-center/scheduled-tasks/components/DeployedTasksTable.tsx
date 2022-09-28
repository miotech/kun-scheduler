import React, { FC, memo, useCallback, useMemo } from 'react';
import { Table, Tooltip, Typography } from 'antd';
import moment from 'moment';
import SafeUrlAssembler from 'safe-url-assembler';
import { Link } from 'umi';
import useI18n from '@/hooks/useI18n';
import useCronSemanticText from '@/hooks/useCronSemanticText';
import { KunSpin } from '@/components/KunSpin';
import LogUtils from '@/utils/logUtils';
import { DeployedTask } from '@/definitions/DeployedTask.type';
import { ColumnProps } from 'antd/es/table';
import { StatusText } from '@/components/StatusText';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { UsernameText } from '@/components/UsernameText';
import TextContainer from '@/components/TextContainer/TextContainer';
import useRedux from '@/hooks/useRedux';
import styles from './DeployedTasksTable.less';

interface DeployedTasksTableProps {
  selectedTask: DeployedTask | null;
  setSelectedTask: (deployedTask: DeployedTask | null) => any;
}

const { Text } = Typography;

const logger = LogUtils.getLoggers('DeployedTasksTableComp');

export const DeployedTasksTable: FC<DeployedTasksTableProps> = memo((props) => {
  const t = useI18n();

  const {
    selector: { loading, pageNum, pageSize, total, tableData },
    dispatch,
  } = useRedux(s => ({
    tableData: s.scheduledTasks.deployedTasks;
    loading: s.loading.effects.scheduledTasks.fetchScheduledTasks,
    pageNum: s.scheduledTasks.filters.pageNum,
    pageSize: s.scheduledTasks.filters.pageSize,
    total: s.scheduledTasks.totalCount,
  }));

  const { selectedTask, setSelectedTask } = props;


  const onChangePagination = useCallback((nextPage: number, nextPageSize?: number) => {
    dispatch.scheduledTasks.updateFilter({
      pageNum: nextPage,
      pageSize: nextPageSize,
    });
  }, [dispatch]);

  const handleRowEvents = useCallback(
    (record: DeployedTask) => {
      return {
        onClick: () => {
          logger.trace('record = %o;', record);
          setSelectedTask(record);
        },
      };
    },
    [setSelectedTask],
  );

  const columns = useMemo<ColumnProps<DeployedTask>[]>(
    () => [
      // Column: Deployed task name
      {
        title: t('scheduledTasks.property.name'),
        dataIndex: 'name',
        key: 'name',
        render: (txt: string, record: DeployedTask) => {
          return (
            <Link
              to={SafeUrlAssembler()
                .template('/operation-center/scheduled-tasks/:id')
                .param({ id: record.id })
                .toString()}
            >
              <TextContainer maxWidth={500}>{txt}</TextContainer>
            </Link>
          );
        },
      },
      // Column: Owner
      {
        title: t('scheduledTasks.property.owner'),
        dataIndex: 'owner',
        key: 'owner',
        render: (txt: string) => (txt ? <UsernameText owner={txt} /> : ''),
      },
      // Column: Last run time
      {
        title: t('scheduledTasks.property.lastRunTime'),
        dataIndex: ['latestTaskRun', 'startAt'],
        key: 'latestRunTime',
        render: (txt: string) => (txt ? moment(txt).format('YYYY-MM-DD HH:mm:ss') : '-'),
        width: 200,
      },
      // Column: Last run status
      {
        title: t('scheduledTasks.property.lastRunStatus'),
        dataIndex: ['latestTaskRun', 'status'],
        key: 'latestRunStatus',
        render: (txt: RunStatusEnum) => (txt ? <StatusText status={txt} /> : '-'),
        width: 120,
      },
      // Column: Cron expression
      {
        title: t('scheduledTasks.property.cronExpression'),
        dataIndex: ['taskPayload', 'scheduleConfig', 'cronExpr'],
        key: 'cronExpr',
        width: 200,
        render: (txt: string) => {
          return (
            // eslint-disable-next-line react-hooks/rules-of-hooks
            <Tooltip >
              <Text code>{txt}</Text>
            </Tooltip>
          );
        },
      },
    ],
    [t]
  );



  return (
    <KunSpin spinning={loading}>
      <Table<DeployedTask>
        className={styles.DeployedTasksTable}
        columns={columns}
        dataSource={tableData}
        rowKey={r => `${r.id}`}
        size="small"
        pagination={{
          current: pageNum,
          pageSize,
          onChange: onChangePagination,
          size: 'small',
          showSizeChanger: true,
          total,
          showTotal: (_total: number) => t('common.pagination.showTotal', { total: _total }),
        }}
        rowSelection={{
          selectedRowKeys: selectedTask ? [selectedTask.id] : [],
          hideSelectAll: true,
          type: 'radio',
          onSelect: record => {
            setSelectedTask(record);
          },
        }}
        onRow={handleRowEvents}
      />
    </KunSpin>
  );
});

