import React, { memo, useState } from 'react';
import { Form, Input, Modal, notification, message } from 'antd';
import useI18n from '@/hooks/useI18n';
import { useHistory } from 'umi';
import uniq from 'lodash/uniq';
import { fetchDeployedTasks } from '@/services/task-deployments/deployed-tasks';
import { createAndRunBackfill } from '@/services/data-backfill/backfill.services';

interface OwnProps {
  visible?: boolean;
  onCancel?: () => any;
  selectedTaskDefIds?: string[];
  initValue?: string;
}

type Props = OwnProps;

export const ConfirmBackfillCreateModal: React.FC<Props> = memo(function ConfirmBackfillCreateModal(props) {
  const { visible, onCancel, selectedTaskDefIds = [], initValue } = props;

  const t = useI18n();
  const history = useHistory();

  const [form] = Form.useForm<{
    name: string;
  }>();

  const [confirmLoading, setConfirmLoading] = useState<boolean>(false);

  const handleClickTask = (id: string, e: React.MouseEvent) => {
    e.preventDefault();
    history.push(`/operation-center/backfill-tasks/${id}`);
  };

  const handleClickRunBackfill = async (name1: string, taskDefIds: string[]) => {
    try {
      const relatedDeployedTasks = await fetchDeployedTasks({
        definitionIds: taskDefIds,
      });
      const relatedWorkflowIds = uniq(
        (relatedDeployedTasks?.records || []).map(deployedTask => deployedTask.workflowTaskId),
      );
      if (relatedWorkflowIds.length !== taskDefIds.length) {
        message.error(t('dataDevelopment.runBackfillTaskDefNotPublishedMessage'));
        return;
      }
      const resp = await createAndRunBackfill({
        name: name1,
        workflowTaskIds: relatedWorkflowIds,
        taskDefinitionIds: taskDefIds,
      });

      const { id, name } = resp;

      notification.open({
        message: t('backfill.create.notification.title'),
        description: (
          <span>
            {t('backfill.create.notification.desc')}{' '}
            <a href={`/operation-center/backfill-tasks/${id}`} onClick={e => handleClickTask(id, e)}>
              {name}
            </a>
          </span>
        ),
      });
    } catch (e) {
      message.error('Run backfill failed.');
    }
  };
  const handleOk = async () => {
    try {
      setConfirmLoading(true);
      const name = await form.getFieldValue('name');
      await handleClickRunBackfill(name, selectedTaskDefIds);
      if (onCancel) {
        form.resetFields();
        onCancel();
      }
    } finally {
      setConfirmLoading(false);
    }
  };
  return (
    <Modal
      visible={visible}
      title={t('dataDevelopment.runBackfill')}
      onCancel={() => {
        if (onCancel) {
          form.resetFields();
          onCancel();
        }
      }}
      okButtonProps={{
        loading: confirmLoading,
      }}
      onOk={handleOk}
    >
      <Form
        form={form}
        initialValues={{
          name: initValue,
        }}
      >
        <Form.Item name="name" label={t('dataDevelopment.backfillName')} required>
          <Input placeholder={t('dataDevelopment.backfillName')} />
        </Form.Item>
      </Form>
    </Modal>
  );
});
