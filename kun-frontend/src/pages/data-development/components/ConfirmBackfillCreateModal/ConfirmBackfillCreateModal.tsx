import React, { memo, useState } from 'react';
import { Form, Input, Modal, notification, message, Select, Radio } from 'antd';
import useI18n from '@/hooks/useI18n';
import { useHistory } from 'umi';
import uniq from 'lodash/uniq';
import { fetchDeployedTasks } from '@/services/task-deployments/deployed-tasks';
import { createAndRunBackfill } from '@/services/data-backfill/backfill.services';
import timeZoneMapList from '@/pages/data-development/task-definition-config/components/timezoneMap';
import { OneshotDatePicker } from '@/pages/data-development/task-definition-config/components/OneshotDatePicker';

const { Option } = Select;

interface OwnProps {
  visible?: boolean;
  onCancel?: () => any;
  selectedTaskDefIds?: string[];
  initValue?: string;
}

type Props = OwnProps;

const getDefaultTimeZone = () => {
  const offsetRate = -(new Date().getTimezoneOffset() / 60);
  const timeZoneDefaultValue = timeZoneMapList.find(i => i.offset === offsetRate)?.value ?? 'UTC';
  return timeZoneDefaultValue;
};

export const ConfirmBackfillCreateModal: React.FC<Props> = memo(function ConfirmBackfillCreateModal(props) {
  const { visible, onCancel, selectedTaskDefIds = [], initValue } = props;
  const defaultTimeZone = getDefaultTimeZone();
  const t = useI18n();
  const history = useHistory();

  const [form] = Form.useForm<{
    name: string;
  }>();

  const [confirmLoading, setConfirmLoading] = useState<boolean>(false);
  const [isScheduleConfig, setIsScheduleConfig] = useState<boolean>(false);

  const initFormValue = {
    name: initValue,
    timeZone: defaultTimeZone,
    cronExpr: null,
  };

  const handleClickTask = (id: string, e: React.MouseEvent) => {
    e.preventDefault();
    history.push(`/operation-center/backfill-tasks/${id}`);
  };

  const handleClickRunBackfill = async (name1: string, taskDefIds: string[], timeZone: string, cronExpr: string) => {
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
        timeZone: isScheduleConfig ? timeZone : null,
        cronExpr: isScheduleConfig ? cronExpr : null,
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
      const timeZone = await form.getFieldValue('timeZone');
      const cronExpr = await form.getFieldValue('cronExpr');

      await handleClickRunBackfill(name, selectedTaskDefIds, timeZone, cronExpr);
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
      width={700}
    >
      <Form form={form} initialValues={initFormValue} labelCol={{ span: 8 }}>
        <Form.Item name="name" label={t('dataDevelopment.backfillName')} required>
          <Input placeholder={t('dataDevelopment.backfillName')} />
        </Form.Item>

        <Form.Item label={t('dataDevelopment.setBackfillTime')}>
          <Radio.Group onChange={(e: any) => setIsScheduleConfig(e.target.value)}>
            <Radio value>{t('common.yes')}</Radio>
            <Radio value={false}>{t('common.no')}</Radio>
          </Radio.Group>
        </Form.Item>
        {isScheduleConfig && (
          <Form.Item label={t('dataDevelopment.businessTime')}>
            <Form.Item
              style={{ display: 'inline-block', width: 180, marginRight: 20 }}
              name="timeZone"
              rules={[{ required: true, message: t('dataDevelopment.definition.scheduleConfig.timeZone') }]}
            >
              <Select
                placeholder={t('dataDevelopment.definition.scheduleConfig.timeZone')}
                style={{ width: '100%' }}
                dropdownMatchSelectWidth={false}
                showSearch
                optionFilterProp="children"
              >
                {timeZoneMapList.map(i => (
                  <Option key={i.value} value={i.value}>
                    {i.label}
                  </Option>
                ))}
              </Select>
            </Form.Item>

            <Form.Item
              style={{ display: 'inline-block', width: 'calc(100% - 220px)' }}
              name="cronExpr"
              initialValue={initFormValue.cronExpr}
            >
              <OneshotDatePicker style={{ width: 220 }} placeholder={t('dataDevelopment.businessTime.select')} />
            </Form.Item>
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
});
