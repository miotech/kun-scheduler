import React, { memo, useMemo, useState } from 'react';
import { Button, Form, Input, Modal, Popconfirm } from 'antd';
import { TaskDefinitionViewBase, TaskDefinitionViewUpdateVO } from '@/definitions/TaskDefinitionView.type';
import useI18n from '@/hooks/useI18n';
import { useUnmount } from 'ahooks';

interface OwnProps {
  mode: 'create' | 'edit';
  initView?: TaskDefinitionViewBase | null;
  visible?: boolean;
  onOk?: (updateVO: TaskDefinitionViewUpdateVO) => any;
  onCancel?: () => any;
  onDelete?: (viewId: string) => any;
}

type Props = OwnProps;

export const TaskDefViewModificationModal: React.FC<Props> = memo(function TaskDefViewModificationModal(props) {
  const {
    visible,
    mode,
    onOk,
    onCancel,
    onDelete,
    initView,
  } = props;

  const t = useI18n();

  const [ waitingConfirm, setWaitingConfirm ] = useState<boolean>(false);
  const [ waitingDelete, setWaitingDelete ] = useState<boolean>(false);

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

  const footerDOM = useMemo(() => {
    if (mode === 'create') {
      return undefined;
    }
      return [
        <Button key="cancel-btn" onClick={onCancel}>
          {t('common.button.cancel')}
        </Button>,
        <Popconfirm
          key="delete-btn"
          title={t('dataDevelopment.taskDefView.deleteAlert.title')}
          okButtonProps={{
            danger: true,
          }}
          onConfirm={async () => {
            if (onDelete && initView) {
              setWaitingDelete(true);
              await onDelete(initView.id);
              setWaitingDelete(false);
            }
          }}
        >
          <Button danger disabled={waitingConfirm} loading={waitingConfirm}>
            {t('common.button.delete')}
          </Button>
        </Popconfirm>,
        <Button
          key="confirm-btn"
          type="primary"
          disabled={waitingDelete}
          onClick={async () => {
            if (onOk) {
              setWaitingConfirm(true);
              await onOk({
                name: form.getFieldValue('name'),
                includedTaskDefinitionIds: [],
              });
              setWaitingConfirm(false);
            }
          }}
          loading={waitingConfirm}
        >
          {t('common.button.confirm')}
        </Button>
      ];
  }, [form, initView, mode, onCancel, onDelete, onOk, t, waitingConfirm, waitingDelete]);

  // noinspection RequiredAttributes
  return (
    <Modal
      title={title}
      visible={visible}
      width={650}
      footer={footerDOM}
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
        <Form.Item
          name="name"
          label={t('dataDevelopment.taskDefView.name')}
          required
          initialValue={initView ? initView.name : ''}
        >
          <Input placeholder={t('dataDevelopment.taskDefView.name.placeholder')} />
        </Form.Item>
      </Form>
    </Modal>
  );
});
