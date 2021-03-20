import React, { memo, useMemo, useState } from 'react';
import { Button, Modal, Popconfirm, Space, Table, Typography } from 'antd';
import useI18n from '@/hooks/useI18n';

import { GlobalVariable } from '@/definitions/GlobalVariable.type';
import { SecretHintIcon } from '@/pages/settings/variable-settings/components/SecretHintIcon';
import { DeleteOutlined } from '@ant-design/icons';
import { ColumnsType } from 'antd/es/table';

import css from '../index.less';

interface OwnProps {
  variableList: GlobalVariable[];
  loading?: boolean;
  onUpdateValue: (key: string, newValue: string) => any;
  onDeleteVariable: (key: string) => any;
}

type Props = OwnProps;

export const VariablesTable: React.FC<Props> = memo(function VariablesTable(
  props,
) {
  const {
    variableList = [],
    loading = false,
    onUpdateValue,
    onDeleteVariable,
  } = props;
  const [editingSecretVariableKey, setEditingSecretVariableKey] = useState<
    string | null
  >(null);
  const t = useI18n();

  const columns: ColumnsType<GlobalVariable> = useMemo(
    () => [
      {
        title: t('settings.variableSettings.key'),
        dataIndex: 'key',
        key: 'key',
        sorter: (a, b) => a.key.localeCompare(b.key),
        width: 360,
        render: (txt: unknown, record: GlobalVariable) => {
          return <code>{record.key}</code>;
        },
      },
      {
        title: t('settings.variableSettings.value'),
        dataIndex: 'value',
        key: 'value',
        className: css.KeyValueWrapper,
        render: (txt: unknown, record: GlobalVariable) => {
          return record.encrypted ? (
            <code>
              <span>
                {editingSecretVariableKey === record.key ? '' : '******'}
              </span>
              <Typography.Text
                editable={{
                  onStart() {
                    setEditingSecretVariableKey(record.key);
                  },
                  onChange(nextValue: string) {
                    Modal.confirm({
                      title: t(
                        'settings.variableSettings.updateSecretVariableAlert',
                        {
                          key: record.key,
                        },
                      ),
                      content: (
                        <blockquote className={css.ValueBlockQuote}>
                          <code>{nextValue.trim()}</code>
                        </blockquote>
                      ),
                      async onOk() {
                        await onUpdateValue(record.key, nextValue);
                      },
                    });
                    setEditingSecretVariableKey(null);
                  },
                }}
              />
            </code>
          ) : (
            <code>
              <Typography.Text
                editable={{
                  onChange(nextValue) {
                    if (record.value === nextValue) {
                      return;
                    }
                    onUpdateValue(record.key, nextValue);
                  },
                }}
              >
                {record.value}
              </Typography.Text>
            </code>
          );
        },
      },
      {
        title: () => (
          <Space size={4}>
            <span>{t('settings.variableSettings.isSecret')}</span>
            <SecretHintIcon />
          </Space>
        ),
        sorter: (a, b) => (a.encrypted ? 1 : -1) - (b.encrypted ? 1 : -1),
        align: 'center',
        dataIndex: 'secret',
        key: 'secret',
        width: t('common.lang') === 'zh-CN' ? 100 : 130,
        render: (txt: unknown, record: GlobalVariable) => {
          return record.encrypted ? t('common.yes') : t('common.no');
        },
      },
      {
        title: '',
        key: 'operations',
        width: 100,
        align: 'center',
        render: (txt: unknown, record: GlobalVariable) => {
          return (
            <span>
              <Button.Group size="small">
                <Popconfirm
                  title={t('settings.variableSettings.deleteAlert', {
                    key: record.key,
                  })}
                  okButtonProps={{
                    danger: true,
                  }}
                  onConfirm={() => {
                    onDeleteVariable(record.key);
                  }}
                >
                  <Button icon={<DeleteOutlined />} danger type="link">
                    {t('common.button.delete')}
                  </Button>
                </Popconfirm>
              </Button.Group>
            </span>
          );
        },
      },
    ],
    [t, editingSecretVariableKey, onUpdateValue, onDeleteVariable],
  );

  return (
    <Table<GlobalVariable>
      loading={loading}
      columns={columns}
      dataSource={variableList}
      bordered
      tableLayout="fixed"
      rowKey="key"
      pagination={{
        size: 'small',
        pageSize: 25,
        showTotal: (_total: number) =>
          t('common.pagination.showTotal', { total: _total }),
      }}
    />
  );
});
