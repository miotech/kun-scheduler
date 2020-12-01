import React, { memo, useCallback } from 'react';
import { Form, message, Modal, Select } from 'antd';
import { TaskDefinitionViewBase } from '@/definitions/TaskDefinitionView.type';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  taskDefViews: TaskDefinitionViewBase[];
  onOk?: (selectedTaskViewId: string) => any;
  onCancel?: () => any;
  visible?: boolean;
  currentViewId: string | null;
}

type Props = OwnProps;

export const AddToOtherViewModal: React.FC<Props> = memo(function AddToOtherViewModal(props) {
  const {
    taskDefViews,
    onOk,
    onCancel,
    visible,
    currentViewId,
  } = props;

  const [ form ] = Form.useForm();
  const t = useI18n();

  const handleOk = useCallback(async () => {
    if (onOk) {
      try {
        const { taskDefViewId } = await form.getFieldsValue();
        onOk(taskDefViewId);
        message.success(t('common.operateSuccess'));
      } catch (e) {
        // do nothing
      }
    }
  }, [
    form,
    onOk,
    t,
  ]);

  return (
    <Modal
      visible={visible}
      onCancel={onCancel}
      onOk={handleOk}
      destroyOnClose
      title={t('dataDevelopment.addSelectedTasksToOtherViews')}
    >
      <Form form={form}>
        <Form.Item
          name="taskDefViewId"
          required
          label={t('dataDevelopment.addSelectedTasksToOtherViews.targetViewToAdd')}
        >
          <Select showSearch optionFilterProp="title">
            {taskDefViews.filter(view => {
              return currentViewId !== view.id;
            }).map(view => (
              <Select.Option
                value={view.id}
                title={view.name}
                key={view.id}
              >
                {view.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <div style={{ marginTop: '4px' }}>
          <small>{t('dataDevelopment.addSelectedTasksToOtherViews.comment')}</small>
        </div>
      </Form>
    </Modal>
  );
});
