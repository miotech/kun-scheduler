import React, { FunctionComponent } from 'react';
import { Modal, Input, Form } from 'antd';
import { ModalProps } from 'antd/es/modal';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  onConfirm?: (commitMsg: string) => any;
}

type Props = ModalProps & OwnProps;

export const TaskCommitModal: FunctionComponent<Props> = (props) => {
  const { onConfirm, ...restProps } = props;
  const t = useI18n();
  const [ form ] = Form.useForm();

  return (
    <Modal
      title={t('dataDevelopment.definition.commitModalTitle')}
      okText={t('common.button.commit')}
      width={650}
      maskClosable={false}
      {...restProps}
      onOk={async () => {
        try {
          await form.validateFields();
          if (onConfirm) {
            onConfirm(form.getFieldValue('commitMsg'));
          }
        } catch (e) {
          // Do nothing
        }
      }}
      afterClose={() => {
        form.resetFields();
      }}
    >
      <Form form={form}>
        <Form.Item
          name="commitMsg"
          label={t('dataDevelopment.definition.commitModalMsg')}
          rules={[
            {
              required: true,
            }
          ]}
        >
          <Input.TextArea
            bordered
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
