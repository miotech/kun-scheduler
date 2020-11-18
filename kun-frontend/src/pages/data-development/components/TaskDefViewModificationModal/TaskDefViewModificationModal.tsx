import React, { memo, useMemo } from 'react';
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
      onOk={() => {
        if (onOk) {
          onOk({
            name: form.getFieldValue('name'),
          });
        }
      }}
      onCancel={onCancel}
      destroyOnClose
    >
      <Form form={form}>
        <Form.Item name="name" required>
          <Input placeholder="Please input view name" />
        </Form.Item>
      </Form>
    </Modal>
  );
});
