import React, { memo, useMemo } from 'react';
import moment from 'moment';
import { ColumnProps } from 'antd/es/table';
import { FailedTestCase, AbnormalDataset, Glossary } from '@/services/monitoring-dashboard';
import { Card, Table, Tooltip, Tag } from 'antd';
import { dayjs } from '@/utils/datetime-utils';
import useI18n from '@/hooks/useI18n';
import { TableOnChangeCallback } from '@/definitions/common-types';
import { Link } from 'umi';
import SafeUrlAssembler from 'safe-url-assembler';
import TextContainer from '@/components/TextContainer/TextContainer';
import { UpOutlined, DownOutlined } from '@ant-design/icons';
import TestCaseRuleTable from './TestCaseRuleTable';

import styles from './FailedTestCasesTable.less';

interface OwnProps {
  data: AbnormalDataset[];
  pageNum: number;
  pageSize: number;
  total: number;
  onChange?: TableOnChangeCallback<AbnormalDataset>;
  loading?: boolean;
}

type Props = OwnProps;

const sortFn = (a: any, b: any) => {
  if (!a) {
    return 1;
  }
  if (!b) {
    return -1;
  }
  if (a > b) {
    return 1;
  }
  return -1;
};

export const FailedTestCasesTable: React.FC<Props> = memo(function FailedTestCasesTable(props) {
  const { data, pageNum, pageSize, total, onChange, loading } = props;

  const t = useI18n();

  const childTableColumns: ColumnProps<FailedTestCase>[] = useMemo(
    () => [
      {
        dataIndex: 'caseName',
        key: 'caseName',
        title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.caseName'),
        render: (txt: string, record: FailedTestCase) => {
          return (
            <TextContainer>
              {record.datasetGid ? (
                <Link
                  to={SafeUrlAssembler()
                    .template('/data-discovery/dataset/:datasetId')
                    .param({
                      datasetId: record.datasetGid,
                    })
                    .query({
                      caseId: record.caseId,
                    })
                    .toString()}
                >
                  {txt}
                </Link>
              ) : (
                txt
              )}
            </TextContainer>
          );
        },
      },
      {
        dataIndex: 'status',
        key: 'status',
        width: 100,
        title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.successOrFailed'),
        render: (v: string) => (v ? t(`monitoringDashboard.dataDiscovery.failedTestCasesTable.result.${v}`) : ''),
      },
      {
        dataIndex: 'errorReason',
        key: 'errorReason',
        width: 100,
        title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.result'),
        ellipsis: true,
        render: (errorReason, record: FailedTestCase) => {
          if (errorReason) {
            return (
              <Tooltip title={errorReason} placement="right" overlayClassName={styles.FailedREasonTooltip}>
                <div
                  style={{
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    width: '100%',
                  }}
                >
                  <Link to={`/operation-center/task-run-id/${record.taskRunId}`} target="_blank">
                    {errorReason}
                  </Link>
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
                {record?.ruleRecords?.map(rule => rule.originalValue).join(', ')}
              </div>
            </Tooltip>
          );
        },
      },
      {
        dataIndex: 'updateTime',
        key: 'updateTime',
        width: 160,
        align: 'right',
        sorter: true,
        title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.lastUpdatedTime'),
        render: (txt: number) => dayjs(txt).format('YYYY-MM-DD HH:mm'),
      },
      {
        dataIndex: 'continuousFailingCount',
        key: 'continuousFailingCount',
        width: 160,
        align: 'right',
        sorter: true,
        title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.continuousFailingCount'),
      },
      {
        dataIndex: 'caseOwner',
        key: 'caseOwner',
        width: 160,
        align: 'right',
        title: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.caseOwner'),
      },
    ],
    [t],
  );

  const expandedRowRender = (record: AbnormalDataset) => {
    const useableData = [
      ...record.tasks.map(i => ({
        caseName: 'DataUpdateFailed',
        caseId: i.taskName,
        taskRunId: i.taskRunId,
        updateTime: i.updateTime,
        errorReason: t('monitoringDashboard.dataDiscovery.failedTestCasesTable.resultContent', { name: i.taskName }),
        status: 'FAILED',
      })),
      ...record.cases.map(i => ({ ...i, datasetGid: record.datasetGid })),
    ] as FailedTestCase[];
    return (
      <Table
        style={{ width: '100%' }}
        columns={childTableColumns}
        scroll={{ x: 1000 }}
        rowClassName={styles.childRow}
        rowKey="caseId"
        dataSource={useableData}
        pagination={false}
      />
    );
  };

  const columns: ColumnProps<AbnormalDataset>[] = useMemo(
    () => [
      {
        key: 'ordinal',
        title: '#',
        width: 60,
        render: (_txt: any, _record: AbnormalDataset, index: number) => (
          <span>{(pageNum - 1) * pageSize + index + 1}</span>
        ),
      },
      {
        key: 'datasetName',
        dataIndex: 'datasetName',
        sorter: (a: AbnormalDataset, b: AbnormalDataset) => sortFn(a.datasetName, b.datasetName),
        title: t('monitoringDashboard.dataDiscovery.abnormalDataset.datasetName'),
        render: (txt: string, record: AbnormalDataset) => {
          return (
            <TextContainer ellipsis tooltipTitle={txt} className={styles.dataSetRow}>
              <Link
                to={SafeUrlAssembler()
                  .template('/data-discovery/dataset/:datasetId')
                  .param({
                    datasetId: record.datasetGid,
                  })
                  .toString()}
              >
                {txt}
              </Link>
            </TextContainer>
          );
        },
      },
      {
        key: 'databaseName',
        dataIndex: 'databaseName',
        sorter: (a: AbnormalDataset, b: AbnormalDataset) => sortFn(a.databaseName, b.databaseName),
        width: 140,
        title: t('monitoringDashboard.dataDiscovery.abnormalDataset.databaseName'),
      },
      {
        key: 'glossaries',
        dataIndex: 'glossaries',
        width: 160,
        title: t('monitoringDashboard.dataDiscovery.abnormalDataset.glossary'),
        render: (v: Glossary[]) => {
          return (
            <div className={styles.tag}>
              {v &&
                v.map((item: Glossary) => (
                  <Link key={item.id} to={`/data-discovery/glossary/${item.id}`} target="_blank">
                    <Tag className={styles.item} color="success">
                      {item.name}
                    </Tag>
                  </Link>
                ))}
            </div>
          );
        },
      },
      // {
      //   key: 'datasourceName',
      //   dataIndex: 'datasourceName',
      //   sorter: (a:AbnormalDataset, b:AbnormalDataset) => sortFn(a.datasourceName,b.datasourceName),
      //   width: 120,
      //   title: t('monitoringDashboard.dataDiscovery.abnormalDataset.datasourceName'),
      // },
      {
        key: 'failedCaseCount',
        dataIndex: 'failedCaseCount',
        sorter: (a: AbnormalDataset, b: AbnormalDataset) => sortFn(a.failedCaseCount, b.failedCaseCount),
        width: 140,
        title: t('monitoringDashboard.dataDiscovery.abnormalDataset.failedCaseCount'),
      },
      {
        key: 'lastUpdatedTime',
        dataIndex: 'updateTime',
        width: 150,
        title: t('monitoringDashboard.dataDiscovery.abnormalDataset.lastUpdateTime'),
        render: (v: string) => moment(v).format('YYYY-MM-DD HH:mm'),
      },
    ],
    [pageNum, pageSize, t],
  );

  return (
    <Card bodyStyle={{ padding: '8px' }}>
      <h3>
        {t('monitoringDashboard.dataDiscovery.failedTestCasesTable.title')}
        {!!total && <span style={{ marginLeft: 4 }}>({total})</span>}
      </h3>
      <Table<AbnormalDataset>
        loading={loading}
        className={styles.table}
        expandable={{
          expandedRowRender,
          fixed: 'right',
          expandIconColumnIndex: 6,
          expandIcon: ({ expanded, onExpand, record }) =>
            expanded ? (
              <UpOutlined onClick={e => onExpand(record, e)} />
            ) : (
              <DownOutlined onClick={e => onExpand(record, e)} />
            ),
        }}
        scroll={{ x: 800 }}
        dataSource={data}
        size="small"
        columns={columns}
        onChange={onChange as any}
        rowKey="datasetGid"
        pagination={{
          current: pageNum,
          pageSize,
          total,
          simple: true,
        }}
      />
    </Card>
  );
});
