import React, { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Space, Table } from 'antd';
import { useRequest, useUpdateEffect } from 'ahooks';
import { Link } from 'umi';
import { searchTaskDefinition } from '@/services/data-development/task-definitions';
import useI18n from '@/hooks/useI18n';
import { UsernameText } from '@/components/UsernameText';
import dayjs from 'dayjs';
import SafeUrlAssembler from 'safe-url-assembler';
import LogUtils from '@/utils/logUtils';
import { generateAsyncAntdTableRowSelectionProps } from '@/utils/antdTableRowSelectionPropsFactory';

// types
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { ColumnProps } from 'antd/es/table';
import { TableOnChangeCallback } from '@/definitions/common-types';
import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';

// css
import useDebouncedUpdateEffect from '@/hooks/useDebouncedUpdateEffect';
import { TaskTemplateIcon } from '@/components/TaskTemplateIcon/TaskTemplateIcon.component';
import styles from './TaskDefinitionTable.module.less';

interface OwnProps {
  taskDefViewId: string | null;
  filters: DataDevelopmentModelFilter;
  /** Triggers table update when changed */
  updateTime: number;
  onTransferToThisViewClick?: () => any;
  onAddToOtherViewBtnClick?: () => any;
  selectedTaskDefIds?: string[];
  setSelectedTaskDefIds?: (keys: string[]) => any;
}

type Props = OwnProps;

export const logger = LogUtils.getLoggers('TaskDefinitionTable');

export const TaskDefinitionTable: React.FC<Props> = memo(function TaskDefinitionTable(props) {
  const {
    taskDefViewId,
    filters,
    updateTime,
    selectedTaskDefIds,
    setSelectedTaskDefIds,
  } = props;

  const [ pageNum, setPageNum ] = useState<number>(1);
  const [ pageSize, setPageSize ] = useState<number>(25);
  const [ selectedRowKeys, setSelectedRowKeys ] = useState<string[]>([]);
  const t = useI18n();

  const { data, loading, run: doFetch } = useRequest(searchTaskDefinition, {
    manual: true,
  });

  useUpdateEffect(() => {
    // clear selected row keys when view changed
    if (setSelectedTaskDefIds) {
      setSelectedTaskDefIds([]);
    }
    setSelectedRowKeys([]);
  }, [
    taskDefViewId,
  ]);

  useEffect(() => {
    doFetch({
      pageNum,
      pageSize,
      name: filters.name,
      taskTemplateName: filters.taskTemplateName || undefined,
      creatorIds: filters.creatorIds as any,
      viewIds: (taskDefViewId != null) ? [taskDefViewId] : undefined,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    doFetch,
    taskDefViewId,
    filters.taskTemplateName,
    filters.creatorIds,
    pageNum,
    pageSize,
    updateTime,
  ]);

  useDebouncedUpdateEffect(() => {
    doFetch({
      pageNum,
      pageSize,
      name: filters.name,
      taskTemplateName: filters.taskTemplateName || undefined,
      creatorIds: filters.creatorIds as any,
      viewIds: (taskDefViewId != null) ? [taskDefViewId] : undefined,
    });
  }, [
    filters.name,
  ], {
    wait: 500,
  });

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
      render: (txt, record) => (
        <Space size="small">
          <TaskTemplateIcon name={record.taskTemplateName} />
          <span>{record.taskTemplateName}</span>
        </Space>
      ),
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

  const rowKeyMapper = useCallback((record: TaskDefinition) => {
    return `${record.id}`;
  }, []);

  return (
    <div
      className={styles.TaskDefTableWrapper}
      data-tid="task-definition-table-wrapper"
    >
      <header className={styles.TaskDefTableHeading} data-tid="task-definition-table-heading">
        <Space>
          <span className={styles.SelectedItemsCountText} data-tid="selected-items-count">
            {t('common.table.rowSelectionCount', { count: selectedTaskDefIds?.length ?? selectedRowKeys.length })}
          </span>
            <span>
            <Button
              type="link"
              disabled={!(selectedTaskDefIds ?? selectedRowKeys).length}
              onClick={() => { setSelectedRowKeys([]); }}
            >
              {t('common.table.clearAllSelectedItems')}
            </Button>
          </span>
          {/*
          <Button disabled={!selectedRowKeys.length}>
            {t('dataDevelopment.submitAll')}
            <CaretRightOutlined />
          </Button>
          */}
          <Button
            disabled={!(selectedTaskDefIds ?? selectedRowKeys).length}
            onClick={() => {
              if (props.onAddToOtherViewBtnClick) {
                props.onAddToOtherViewBtnClick();
              }
            }}
          >
            {t('dataDevelopment.addSelectedTasksToOtherViews')}
          </Button>
          {(taskDefViewId != null) ? (
            <Button
              disabled={!taskDefViewId}
              onClick={() => {
                if (props.onTransferToThisViewClick) {
                  props.onTransferToThisViewClick();
                }
              }}
            >
              {t('dataDevelopment.editCurrentViewTasks')}
            </Button>
          ) : <></>}
        </Space>
      </header>
      <Table<TaskDefinition>
        className={styles.TaskDefTable}
        data-tid="task-definition-table"
        columns={columns}
        dataSource={data?.records || []}
        rowKey={rowKeyMapper}
        size="small"
        loading={loading}
        onChange={handleTableChange}
        rowSelection={{
          ...generateAsyncAntdTableRowSelectionProps(
            data?.records || [],
            rowKeyMapper,
            selectedTaskDefIds ?? selectedRowKeys,
            setSelectedTaskDefIds ?? setSelectedRowKeys,
          ),
        }}
        pagination={{
          current: pageNum,
          pageSize,
          total: data?.totalCount || 0,
          showTotal: (_total: number) => t('common.pagination.showTotal', { total: _total }),
        }}
      />
    </div>
  );
});
