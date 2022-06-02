import React, { useEffect } from 'react';
import { Modal, Table, message } from 'antd';
import useI18n from '@/hooks/useI18n';
import { dayjs } from '@/utils/datetime-utils';
import { TaskRun } from '@/definitions/TaskRun.type';
import { queryDownstreamTaskruns, restartTaskrun } from '@/services/task-deployments/deployed-tasks';
import { useRequest } from 'ahooks';
import { StatusText } from '@/components/StatusText';

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

interface Props {
  isModalVisible: boolean;
  taskRunId: string;
  setIsModalVisible: (v: boolean) => void;
  refreshTaskRun: () => void;
}
export default function TaskRerunModal(props: Props) {
  const t = useI18n();
  const I18n = 'taskRun.rerunDownstream.table';
  const { isModalVisible, setIsModalVisible, taskRunId, refreshTaskRun } = props;

  const { data, run, loading } = useRequest(queryDownstreamTaskruns, {
    manual: true,
  });

  const { runAsync: restartRun, loading: confirmLoading } = useRequest(restartTaskrun, {
    debounceWait: 500,
    manual: true,
  });

  const submit = async () => {
    if (taskRunId && data?.length) {
      const ids = data?.map(item => item.id);
      const res = await restartRun(ids);
      if (res) {
        message.success(t('common.operateSuccess'));
        setIsModalVisible(false);
        refreshTaskRun();
      }
    }
  };

  useEffect(() => {
    run(taskRunId);
  }, [taskRunId, run]);

  const columns = [
    {
      title: t(`${I18n}.id`),
      dataIndex: 'id',
    },
    {
      title: t(`${I18n}.name`),
      dataIndex: 'name',
      render: (txt: any, record: TaskRun) => {
        return <div>{record?.task?.name}</div>;
      },
    },
    {
      title: t(`${I18n}.planTime`),
      render: (txt: any, record: TaskRun) => {
        return convertScheduledTick(record.scheduledTick?.time);
      },
    },
    {
      title: t(`${I18n}.status`),
      dataIndex: 'status',
      render: (txt: any, record: TaskRun) => {
        return <StatusText status={record.status} />;
      },
    },
  ];
  return (
    <Modal
      title={t(`${I18n}.title`)}
      visible={isModalVisible}
      onCancel={() => setIsModalVisible(false)}
      width={900}
      onOk={submit}
      confirmLoading={confirmLoading}
    >
      <Table columns={columns} dataSource={data} loading={loading} />
    </Modal>
  );
}
