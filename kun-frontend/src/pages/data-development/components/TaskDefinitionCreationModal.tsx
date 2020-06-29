import React, { useCallback } from 'react';
import { Modal, Form, Input } from 'antd';
import useI18n from '@/hooks/useI18n';

export interface TaskDefinitionCreationModalProps {
  visible: boolean;
  taskTemplateName?: string;
  onOk: (name: string) => any;
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

  const handleOnOk = useCallback(() => {
    form.validateFields().then(values => {
      // if form is validate
      const { name } = values;
      onOk(name as string);
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
    >
      <Form form={form} {...layout}>
        <Form.Item
          name="name"
          label={`${t('dataDevelopment.definition.property.name')} (${taskTemplateName || ''})`}
          rules={[{ required: true }]}
        >
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  );
};
