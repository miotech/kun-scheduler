import React, { memo, useMemo } from 'react';
import { Table, Popconfirm, Tag } from 'antd';
import { ColumnsType } from 'antd/lib/table';
import {
  DeleteOutlined,
  CheckCircleFilled,
  CloseCircleFilled,
  StopFilled,
} from '@ant-design/icons';
import {
  DataQualityItem,
  DataQualityHistory,
} from '@/rematch/models/datasetDetail';
import { DataQualityType } from '@/rematch/models/dataQuality';
import useI18n from '@/hooks/useI18n';

import styles from './DataQualityTable.less';

interface Props {
  data: DataQualityItem[];
  onDelete: (id: string) => void;
  onClick: (id: string) => void;
}

const tagColorMap = {
  [DataQualityType.Accuracy]: 'orange',
  [DataQualityType.Completeness]: 'green',
  [DataQualityType.Consistency]: 'blue',
  [DataQualityType.Timeliness]: 'red',
};

const colorMap = {
  warning: '#ff6336',
  green: '#9ac646',
  stop: '#526079',
};

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
        key: 'types',
        dataIndex: 'types',
        title: t('dataDetail.dataQuality.type'),
        render: (types: DataQualityType[] | null) => (
          <div>
            {types &&
              types.map(type => (
                <Tag color={tagColorMap[type]}>
                  {t(`dataDetail.dataQuality.type.${type}`)}
                </Tag>
              ))}
          </div>
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
        key: 'historyList',
        dataIndex: 'historyList',
        title: t('dataDetail.dataQualityTable.historyList'),
        render: (historyList: DataQualityHistory[]) => (
          <div className={styles.historyList}>
            {historyList.map(history => {
              if (history === DataQualityHistory.SUCCESS) {
                return (
                  <CheckCircleFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.green }}
                  />
                );
              }
              if (history === DataQualityHistory.FAILED) {
                return (
                  <CloseCircleFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.warning }}
                  />
                );
              }
              if (history === DataQualityHistory.SKIPPED) {
                return (
                  <StopFilled
                    className={styles.historyIcon}
                    style={{ color: colorMap.stop }}
                  />
                );
              }
              return null;
            })}
          </div>
        ),
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
            <DeleteOutlined />
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
