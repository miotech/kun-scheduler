import React, { useCallback, useState } from 'react';
import { Modal, Form, Input, Radio } from 'antd';
import useI18n from '@/hooks/useI18n';

export interface TaskDefinitionCreationModalProps {
  visible: boolean;
  taskTemplateName?: string;
  onOk: (name: string, createInCurrentView: boolean) => any;
  onCancel: () => any;
}

const layout = {
  labelCol: { span: 8 },
  wrapperCol: { span: 16 },
};

const { useForm } = Form;

export const TaskDefinitionCreationModal: React.FC<TaskDefinitionCreationModalProps> = props => {
  const {
    visible,
    onOk,
    onCancel,
    taskTemplateName,
  } = props;

  const t = useI18n();
  const [ form ] = useForm();
  const [ submitting, setSubmitting ] = useState<boolean>(false);

  const handleOnOk = useCallback(() => {
    form.validateFields().then(async values => {
      // if form is validate
      const { name, createInCurrentView } = values;
      setSubmitting(true);
      try {
        await onOk(name as string, createInCurrentView as boolean);
      } finally {
        setSubmitting(false);
      }
    });
  }, [
    onOk,
    form,
  ]);

  return (
    <Modal
      title={t('dataDevelopment.definition.creationTitle')}
      width={800}
      visible={visible}
      onOk={handleOnOk}
      onCancel={onCancel}
      destroyOnClose
      okButtonProps={{
        loading: submitting,
      }}
    >
      <Form form={form} {...layout}>
        <Form.Item
          name="name"
          label={`${t('dataDevelopment.definition.property.name')} (${taskTemplateName || ''})`}
          rules={[{ required: true }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          name="createInCurrentView"
          label="Create in current view"
          initialValue
        >
          <Radio.Group>
            <Radio value>
              {t('common.yes')}
            </Radio>
            <Radio value={false}>
              {t('common.no')}
            </Radio>
          </Radio.Group>
        </Form.Item>
      </Form>
    </Modal>
  );
};
