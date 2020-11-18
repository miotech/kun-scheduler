import React, { memo, useMemo, useState } from 'react';
import { Form, Input, Modal } from 'antd';
import { TaskDefinitionViewUpdateVO } from '@/definitions/TaskDefinitionView.type';
import useI18n from '@/hooks/useI18n';
import { useUnmount } from 'ahooks';

interface OwnProps {
  mode: 'create' | 'edit';
  visible?: boolean;
  onOk?: (updateVO: TaskDefinitionViewUpdateVO) => any;
  onCancel?: () => any;
}

type Props = OwnProps;

export const TaskDefViewModificationModal: React.FC<Props> = memo(function TaskDefViewModificationModal(props) {
  const {
    visible,
    mode,
    onOk,
    onCancel,
  } = props;

  const t = useI18n();

  const [ waitingConfirm, setWaitingConfirm ] = useState<boolean>(false);
  const [ form ] = Form.useForm<{
    name: string;
  }>();

  useUnmount(() => {
  });

  const title = useMemo(() => {
    if (mode === 'create') {
      return t('dataDevelopment.taskDefView.create');
    }
    return t('dataDevelopment.taskDefView.edit');
  }, [
    mode,
    t,
  ]);

  // noinspection RequiredAttributes
  return (
    <Modal
      title={title}
      visible={visible}
      width={650}
      onOk={async () => {
        if (onOk) {
          setWaitingConfirm(true);
          await onOk({
            name: form.getFieldValue('name'),
            includedTaskDefinitionIds: [],
          });
          setWaitingConfirm(false);
        }
      }}
      onCancel={onCancel}
      afterClose={() => {
        form.resetFields();
      }}
      destroyOnClose
      okButtonProps={{
        loading: waitingConfirm,
      }}
    >
      <Form form={form}>
        <Form.Item name="name" label={t('dataDevelopment.taskDefView.name')} required>
          <Input placeholder={t('dataDevelopment.taskDefView.name.placeholder')} />
        </Form.Item>
      </Form>
    </Modal>
  );
});
