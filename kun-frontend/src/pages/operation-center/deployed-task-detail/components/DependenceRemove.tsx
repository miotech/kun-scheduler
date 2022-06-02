import React, { memo, useState, useEffect } from 'react';
import { Table, message, Modal } from 'antd';
import { StatusText } from '@/components/StatusText';
import { useRequest } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import { queryUpstreamTaskruns, removeTaskrunDependency } from '@/services/task-deployments/deployed-tasks';
import { TaskRun } from '@/definitions/TaskRun.type';
import { dayjs } from '@/utils/datetime-utils';

interface OwnProps {
  visible?: boolean;
  currentId: string | null;
  onCancel: () => any;
  refreshTaskRun: () => any;
}

type Props = OwnProps;

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

export const DependenceRemove: React.FC<Props> = memo(function DependenceRemove(props) {
  const t = useI18n();
  const { visible, currentId, onCancel, refreshTaskRun } = props;
  const [selectedRowKeys, setSelectRowKeys] = useState<string[]>([]);

  const { data, loading, run } = useRequest(queryUpstreamTaskruns, {
    manual: true,
  });
  const { loading: confirmLoading, run: submitRun } = useRequest(removeTaskrunDependency, {
    manual: true,
  });

  useEffect(() => {
    if (currentId) {
      run(currentId);
    }
  }, [currentId, run]);

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: any[]) => setSelectRowKeys(keys),
  };

  const submit = async () => {
    if (currentId && selectedRowKeys.length) {
      await submitRun(currentId, selectedRowKeys);
      message.success(t('common.operateSuccess'));
      onCancel();
      run(currentId);
      refreshTaskRun();
    }
  };

  const columns = [
    {
      title: t('taskRun.dependence.taskName'),
      key: 'name',
      render: (txt: any, record: TaskRun) => {
        return <div>{record.task.name}</div>;
      },
    },
    {
      title: t('taskRun.dependence.status'),
      key: 'status',
      render: (txt: any, record: TaskRun) => {
        return <StatusText status={record.status} />;
      },
    },
    {
      title: t('taskRun.dependence.planTime'),
      key: 'time',
      render: (txt: any, record: TaskRun) => {
        return convertScheduledTick(record.scheduledTick?.time);
      },
    },
  ];

  return (
    <Modal
      visible={visible}
      onCancel={onCancel}
      title={t('taskRun.dependence')}
      onOk={submit}
      width={700}
      confirmLoading={confirmLoading}
    >
      <Table
        rowKey="id"
        pagination={false}
        rowSelection={rowSelection}
        loading={loading}
        columns={columns}
        dataSource={data || []}
      />
    </Modal>
  );
});
