import React, { memo, useCallback, useEffect } from 'react';
import { Form, message, Modal, Select } from 'antd';
import { useRequest } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import { searchTaskDefinitionViews } from '@/services/data-development/task-definition-views';

interface OwnProps {
  onOk?: (selectedTaskViewId: string) => any;
  onCancel?: () => any;
  visible?: boolean;
  currentViewId: string | null;
}

type Props = OwnProps;

export const AddToOtherViewModal: React.FC<Props> = memo(function AddToOtherViewModal(props) {
  const { onOk, onCancel, visible, currentViewId } = props;

  const [form] = Form.useForm();
  const t = useI18n();

  const { data: searchTaskDefinitionViewsRecords, run: doFetch } = useRequest(searchTaskDefinitionViews, {
    debounceWait: 300,
    manual: true,
  });

  useEffect(() => {
    doFetch({
      pageNumber: 1,
      pageSize: 100,
    });
  }, [doFetch]);

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
  }, [form, onOk, t]);
  const onSearch = useCallback(
    async (value: string) => {
      doFetch({
        pageNumber: 1,
        pageSize: 100,
        keyword: value,
      });
    },
    [doFetch],
  );
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
          <Select onSearch={onSearch} showSearch optionFilterProp="title">
            {(
              searchTaskDefinitionViewsRecords?.records.filter(view => {
                return currentViewId !== view.id;
              }) ?? []
            ).map(view => (
              <Select.Option value={view.id} title={view.name} key={view.id}>
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
