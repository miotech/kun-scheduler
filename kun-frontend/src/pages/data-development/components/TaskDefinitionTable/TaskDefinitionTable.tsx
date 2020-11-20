import React, { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { Table } from 'antd';
import { useRequest } from 'ahooks';
import { Link } from 'umi';
import { searchTaskDefinition } from '@/services/data-development/task-definitions';
import useI18n from '@/hooks/useI18n';
import { UsernameText } from '@/components/UsernameText';
import dayjs from 'dayjs';

// types
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { ColumnProps } from 'antd/es/table';
import { TableOnChangeCallback } from '@/definitions/common-types';

// css
import SafeUrlAssembler from 'safe-url-assembler';
import styles from './TaskDefinitionTable.module.less';

interface OwnProps {
  taskDefViewId: string | null;
}

type Props = OwnProps;

export const TaskDefinitionTable: React.FC<Props> = memo(function TaskDefinitionTable(props) {
  const {
    taskDefViewId,
  } = props;

  const [ pageNum, setPageNum ] = useState<number>(1);
  const [ pageSize, setPageSize ] = useState<number>(20);
  const t = useI18n();

  const { data, loading, run: doFetch } = useRequest(searchTaskDefinition, {
    manual: true,
  });

  useEffect(() => {
    doFetch({
      pageNum,
      pageSize,
      viewId: (taskDefViewId != null) ? taskDefViewId : undefined,
    });
  }, [
    taskDefViewId,
    pageNum,
    pageSize,
    doFetch,
  ]);

  const columns: ColumnProps<TaskDefinition>[] = useMemo(() => [
    {
      key: 'name',
      title: t('dataDevelopment.definition.property.name'),
      dataIndex: 'name',
      render: ((txt, record) =>
        <Link
          to={SafeUrlAssembler(`/data-development/task-definition/:taskDefId`)
            .param({
              taskDefId: record.id,
            })
            .toString()
          }
        >
          {record.name}
        </Link>
      ),
    },
    {
      key: 'taskTemplateName',
      width: 180,
      title: t('dataDevelopment.definition.property.taskTemplateName'),
      dataIndex: 'taskTemplateName',
    },
    {
      key: 'owner',
      width: 160,
      title: t('dataDevelopment.definition.property.owner'),
      dataIndex: 'owner',
      render: (txt: any, record: TaskDefinition) => (
        <UsernameText
          userId={record.owner}
        />
      ),
    },
    {
      key: 'createTime',
      width: 200,
      title: t('dataDevelopment.definition.property.createTime'),
      dataIndex: 'createTime',
      render: (txt: any, record: TaskDefinition) => (
        (record.createTime != null) ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'
      ),
    },
    {
      key: 'lastUpdateTime',
      width: 200,
      title: t('dataDevelopment.definition.property.lastUpdateTime'),
      dataIndex: 'lastUpdateTime',
      render: (txt: any, record: TaskDefinition) => (
        (record.createTime != null) ? dayjs(record.lastUpdateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
      ),
    },
    {
      key: 'isDeployed',
      width: 80,
      title: t('dataDevelopment.definition.property.isDeployed'),
      dataIndex: 'isDeployed',
      render: (txt: any, record: TaskDefinition) => (
        (record.isDeployed) ? t('common.yes') : t('common.no')
      ),
    },
  ], [
    t,
  ]);

  const handleTableChange: TableOnChangeCallback<TaskDefinition> = useCallback((pagination) => {
    setPageNum(pagination.current || 1);
    setPageSize(pagination.pageSize || 20);
  }, []);

  return (
    <Table<TaskDefinition>
      className={styles.TaskDefTable}
      data-tid="task-definition-table"
      columns={columns}
      dataSource={data?.records || []}
      rowKey="id"
      size="small"
      bordered
      loading={loading}
      onChange={handleTableChange}
      rowSelection={{

      }}
      pagination={{
        current: pageNum,
        pageSize,
        total: data?.totalCount || 0,
        showTotal: (_total: number) => t('common.pagination.showTotal', { total: _total }),
      }}
    />
  );
});
