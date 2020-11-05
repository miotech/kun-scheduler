import React, { memo, useMemo } from 'react';
import { Table, Tooltip } from 'antd';
import { ColumnProps } from 'antd/lib/table';
import {
  DataQualityHistory,
  DataQualityHistoryStatus,
} from '@/rematch/models/dataQuality';
import useI18n from '@/hooks/useI18n';
import { watermarkFormatter } from '@/utils/glossaryUtiles';
import TestCaseRuleTable from '@/pages/monitoring-dashboard/components/data-discovery-board/TestCaseRuleTable';
import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons';

import styles from './HistoryTable.less';

const colorMap = {
  warning: '#ff6336',
  green: '#9ac646',
};

interface Props {
  data: DataQualityHistory[];
  loading: boolean;
}

export default memo(function HistoryTable({ data, loading }: Props) {
  const t = useI18n();

  const columns: ColumnProps<DataQualityHistory>[] = useMemo(
    () => [
      {
        title: t('dataQualityModal.historyTable.status'),
        dataIndex: 'status',
        width: 50,
        render: (status: DataQualityHistoryStatus) => {
          if (status === DataQualityHistoryStatus.SUCCESS) {
            return <CheckCircleFilled style={{ color: colorMap.green }} />;
          }
          return <CloseCircleFilled style={{ color: colorMap.warning }} />;
        },
      },
      {
        title: t('dataQualityModal.historyTable.result'),
        dataIndex: 'errorReason',
        width: 150,
        ellipsis: true,
        render: (errorReason: string, record: DataQualityHistory) => {
          if (errorReason) {
            return (
              <Tooltip
                title={errorReason}
                placement="right"
                overlayClassName={styles.FailedREasonTooltip}
              >
                <div
                  style={{
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    width: '100%',
                  }}
                >
                  {errorReason}
                </div>
              </Tooltip>
            );
          }
          return (
            <Tooltip
              title={<TestCaseRuleTable data={record.ruleRecords} />}
              placement="right"
              color="#ffffff"
              overlayClassName={styles.TestCaseRuleTableTooltip}
            >
              <div
                style={{
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                  width: '100%',
                }}
              >
                {record.ruleRecords.map(rule => rule.originalValue).join(', ')}
              </div>
            </Tooltip>
          );
        },
      },
      {
        title: t('dataQualityModal.historyTable.updateTime'),
        dataIndex: 'updateTime',
        width: 100,
        render: (updateTime: number) => watermarkFormatter(updateTime),
      },
      {
        title: t('dataQualityModal.historyTable.continuousFailingCount'),
        dataIndex: 'continuousFailingCount',
        width: 100,
      },
    ],
    [t],
  );

  return (
    <Table<DataQualityHistory>
      style={{ width: '100%' }}
      dataSource={data}
      columns={columns}
      pagination={false}
      size="small"
      loading={loading}
    />
  );
});
