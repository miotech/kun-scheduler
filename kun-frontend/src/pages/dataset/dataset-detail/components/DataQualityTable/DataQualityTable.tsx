import React, { memo, useMemo } from 'react';
import { Table, Popconfirm } from 'antd';
import { ColumnsType } from 'antd/lib/table';
import { CloseOutlined } from '@ant-design/icons';
import { DataQualityItem } from '@/rematch/models/datasetDetail';
import useI18n from '@/hooks/useI18n';

import styles from './DataQualityTable.less';

interface Props {
  data: DataQualityItem[];
  onDelete: (id: string) => void;
  onClick: (id: string) => void;
}

export default memo(function DataQualityTable({
  data,
  onDelete,
  onClick,
}: Props) {
  const t = useI18n();

  const columns: ColumnsType<DataQualityItem> = useMemo(
    () => [
      {
        key: 'name',
        dataIndex: 'name',
        title: t('dataDetail.dataQualityTable.name'),
        className: styles.nameColumn,
        width: 280,
        render: (name: string, record) => (
          <span
            className={styles.pointerLabel}
            onClick={() => onClick(record.id)}
          >
            {name}
          </span>
        ),
      },
      {
        key: 'updater',
        dataIndex: 'updater',
        title: t('dataDetail.dataQualityTable.updater'),
        className: styles.nameColumn,
        width: 100,
      },
      {
        key: 'operator',
        dataIndex: 'id',
        width: 30,
        render: (id: string) => (
          <Popconfirm
            title={t('dataDetail.dataquality.delete.title')}
            onConfirm={() => onDelete(id)}
            okText={t('common.button.confirm')}
            cancelText={t('common.button.cancel')}
          >
            <CloseOutlined />
          </Popconfirm>
        ),
      },
    ],
    [onClick, onDelete, t],
  );

  return (
    <Table
      className={styles.dataQualityTable}
      columns={columns}
      dataSource={data}
      pagination={false}
      onHeaderRow={() => ({
        className: styles.header,
      })}
      size="small"
    />
  );
});
