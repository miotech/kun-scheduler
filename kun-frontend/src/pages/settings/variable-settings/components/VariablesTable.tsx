import React, { memo, useMemo } from 'react';
import { Table } from 'antd';
import useI18n from '@/hooks/useI18n';

import { GlobalVariable } from '@/definitions/GlobalVariable.type';

interface OwnProps {
  variableList: GlobalVariable[];
  loading?: boolean;
}

type Props = OwnProps;

export const VariablesTable: React.FC<Props> = memo(function VariablesTable(
  props,
) {
  const { variableList = [], loading = false } = props;
  const t = useI18n();

  const columns = useMemo(
    () => [
      {
        title: t('settings.variableSettings.key'),
        dataIndex: 'key',
        key: 'key',
        render: (txt: any, record: GlobalVariable) => {
          return <code>{record.key}</code>;
        },
      },
      {
        title: t('settings.variableSettings.value'),
        dataIndex: 'value',
        key: 'value',
        render: (txt: any, record: GlobalVariable) => {
          return record.encrypted ? (
            <code>******</code>
          ) : (
            <code>{record.value}</code>
          );
        },
      },
      {
        title: t('settings.variableSettings.isSecret'),
        dataIndex: 'secret',
        key: 'secret',
        width: 80,
        render: (txt: any, record: GlobalVariable) => {
          return record.encrypted ? t('common.yes') : t('common.no');
        },
      },
    ],
    [t],
  );

  return (
    <Table<GlobalVariable>
      loading={loading}
      columns={columns}
      dataSource={variableList}
      bordered
      rowKey="key"
      size="small"
      pagination={{
        size: 'small',
        pageSize: 50,
      }}
    />
  );
});
