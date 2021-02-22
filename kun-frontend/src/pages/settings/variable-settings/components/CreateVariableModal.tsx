import React, { memo, useCallback, useMemo } from 'react';
import { Form, Input, Modal, Space, Switch } from 'antd';
import useI18n from '@/hooks/useI18n';
import { SecretHintIcon } from '@/pages/settings/variable-settings/components/SecretHintIcon';
import { Rule } from 'rc-field-form/lib/interface';

interface OwnProps {
  visible: boolean;
  onConfirm: (formValues: ModalFormValue) => any;
  onCancel: () => any;
  existingVariableKeys?: string[];
}

export type ModalFormValue = {
  key: string;
  value: string;
  secret: boolean;
};

type Props = OwnProps;

export const CreateVariableModal: React.FC<Props> = memo(
  function CreateVariableModal(props) {
    const { visible, onCancel, onConfirm, existingVariableKeys = [] } = props;

    const t = useI18n();

    const [form] = Form.useForm();

    const formLayout = useMemo(
      () => ({
        labelCol: { flex: '0 0 90px' },
        wrapperCol: { flex: '1 1' },
      }),
      [],
    );

    const duplicateKeyRule: Rule = useMemo(() => {
      return {
        async validator(rule, value) {
          if (existingVariableKeys.indexOf(`${value}`.trim()) >= 0) {
            throw new Error(
              t('settings.variableSettings.duplicateKeyAlert', {
                key: `${value}`,
              }),
            );
          }
        },
      } as Rule;
    }, [existingVariableKeys]);

    const handleCancel = useCallback(() => {
      onCancel();
      form.resetFields();
    }, [onCancel, form]);

    const handleOk = useCallback(async () => {
      let values: ModalFormValue | null = null;
      try {
        await form.validateFields();
        values = (await form.getFieldsValue()) as ModalFormValue;
      } finally {
        // Do nothing
      }
      if (values && onConfirm) {
        await onConfirm(values);
        handleCancel();
      }
    }, [form, handleCancel, onConfirm]);

    return (
      <Modal
        width={800}
        visible={visible}
        title={t('settings.variableSettings.create')}
        onOk={handleOk}
        onCancel={handleCancel}
        maskClosable={false}
      >
        <Form {...formLayout} form={form}>
          <Form.Item
            label={t('settings.variableSettings.key')}
            name="key"
            required
            rules={[{ required: true }, duplicateKeyRule]}
          >
            <Input />
          </Form.Item>
          <Form.Item label={t('settings.variableSettings.value')} name="value">
            <Input.TextArea
              onKeyPress={ev => {
                if (ev.key === 'Enter') {
                  ev.preventDefault();
                }
              }}
            />
          </Form.Item>
          <Form.Item
            label={
              <Space size={4}>
                <span>{t('settings.variableSettings.isSecret')}</span>
                <SecretHintIcon />
              </Space>
            }
            valuePropName="checked"
            name="secret"
          >
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    );
  },
);
