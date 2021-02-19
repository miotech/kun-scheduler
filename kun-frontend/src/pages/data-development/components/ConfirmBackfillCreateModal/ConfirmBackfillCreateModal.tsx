import React, { memo, useCallback, useState } from 'react';
import { Form, Input, Modal } from 'antd';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  visible?: boolean;
  onConfirm: (name: string, selectedTaskDefIds: string[]) => any;
  onCancel?: () => any;
  selectedTaskDefIds?: string[];
}

type Props = OwnProps;

export const ConfirmBackfillCreateModal: React.FC<Props> = memo(
  function ConfirmBackfillCreateModal(props) {
    const { visible, onCancel, onConfirm, selectedTaskDefIds = [] } = props;

    const t = useI18n();

    const [form] = Form.useForm<{
      name: string;
    }>();

    const [confirmLoading, setConfirmLoading] = useState<boolean>(false);

    const handleOk = useCallback(async () => {
      if (onConfirm) {
        try {
          setConfirmLoading(true);
          const name = await form.getFieldValue('name');
          await onConfirm(name, selectedTaskDefIds);
          if (onCancel) {
            form.resetFields();
            onCancel();
          }
        } finally {
          setConfirmLoading(false);
        }
      }
    }, [form, onCancel, onConfirm, selectedTaskDefIds]);

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
        <Form form={form}>
          <Form.Item
            name="name"
            label={t('dataDevelopment.backfillName')}
            required
          >
            <Input placeholder={t('dataDevelopment.backfillName')} />
          </Form.Item>
        </Form>
      </Modal>
    );
  },
);
