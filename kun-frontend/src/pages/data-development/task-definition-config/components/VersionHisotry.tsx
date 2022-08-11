import React, { useState, memo, useMemo, useCallback } from 'react';
import useI18n from '@/hooks/useI18n';
import { Table, Button } from 'antd';
import { UsernameText } from '@/components/UsernameText';
import { TaskCommit } from '@/definitions/TaskDefinition.type';
import moment from 'moment';
import useUrlState from '@ahooksjs/use-url-state';
import { useMount } from 'ahooks';
import { DiffVersion } from './DiffVersion';
import { ViewVersion } from './ViewVersion';
import Styles from './VersionHistory.less';

interface Props {
  taskCommits: TaskCommit[];
}

export const VersionHistory: React.FC<Props> = memo(props => {
  const t = useI18n();
  const { taskCommits } = props;
  const [selectedRowKeys, setSelectRowkeys] = useState<React.Key[]>([]);
  const [selectedRows, setSelectRows] = useState<TaskCommit[]>([]);
  const [viewTaskCommit, setViewTaskCommit] = useState<TaskCommit | null>(null);
  const [query, setQuery] = useUrlState({ tab: '' });

  const setView = (commit: TaskCommit) => {
    setQuery({ tab: 'viewVersion' });
    setViewTaskCommit(commit);
  };
  const setDiff = (selecteds: TaskCommit[]) => {
    setQuery({ tab: 'diffVersion' });
    setSelectRows(selecteds);
  };

  const viewVersionChange = useCallback(
    (commit: TaskCommit) => {
      const findIndex = taskCommits.findIndex(item => item.id === commit.id);
      const diffVersion = taskCommits[findIndex + 1];
      setQuery({ tab: 'diffVersion' });
      setSelectRows([commit, diffVersion]);
    },
    [setQuery, taskCommits],
  );

  useMount(() => {
    setQuery({ tab: undefined });
  });
  const columns = useMemo(
    () => [
      {
        title: t('dataDevelopment.definition.version'),
        dataIndex: 'version',
      },
      {
        title: t('dataDevelopment.definition.committedAt'),
        dataIndex: 'committedAt',
        render: (txt: any, record: TaskCommit) => {
          return <div>{record?.committedAt ? moment(record.committedAt).format('YYYY-MM-DD HH:mm:ss') : '...'} </div>;
        },
      },
      {
        title: t('dataDevelopment.definition.committer'),
        dataIndex: 'committer',
        render: (txt: any, record: TaskCommit) => {
          return <UsernameText owner={record?.committer} />;
        },
      },
      {
        title: t('dataDevelopment.definition.commitModalMsg'),
        dataIndex: 'message',
      },
      {
        title: t('common.column.action'),
        render: (txt: any, record: TaskCommit) => {
          return (
            <>
              {record.version !== 'V1' && (
                <span
                  style={{ color: '#1A73E8', cursor: 'pointer', marginRight: '10px' }}
                  onClick={() => {
                    viewVersionChange(record);
                  }}
                >
                  {t('dataDevelopment.definition.version.viewChange')}
                </span>
              )}
              <span
                style={{ color: '#1A73E8', cursor: 'pointer' }}
                onClick={() => {
                  setViewTaskCommit(record);
                  setQuery({ tab: 'viewVersion' });
                }}
              >
                {t('dataDevelopment.definition.version.viewConfig')}
              </span>
            </>
          );
        },
      },
    ],
    [t, setQuery, viewVersionChange],
  );
  const rowSelection = {
    selectedRowKeys,
    hideSelectAll: true,
    onChange: (keys: React.Key[], selecteds: TaskCommit[]) => {
      setSelectRowkeys(keys);
      setSelectRows(selecteds);
    },
    getCheckboxProps: (record: TaskCommit) => {
      return {
        disabled: selectedRowKeys.length === 2 && !selectedRowKeys.includes(record.id),
      };
    },
  };

  return (
    <div className={Styles.content}>
      {query.tab === 'viewVersion' && viewTaskCommit && (
        <ViewVersion
          setView={(taskCommit: TaskCommit) => setView(taskCommit)}
          setDiff={(selecteds: TaskCommit[]) => setDiff(selecteds)}
          taskCommits={taskCommits}
          viewTaskCommit={viewTaskCommit}
          goBack={() => setQuery({ tab: undefined })}
        />
      )}
      {query.tab === 'diffVersion' && !!selectedRows.length && (
        <DiffVersion
          setView={(taskCommit: TaskCommit) => setView(taskCommit)}
          diffRows={selectedRows}
          goBack={() => setQuery({ tab: undefined })}
        />
      )}
      {!query.tab && (
        <div style={{ backgroundColor: '#fff', padding: '20px 30px' }}>
          <Button
            disabled={selectedRowKeys.length !== 2}
            type="primary"
            onClick={() => setQuery({ tab: 'diffVersion' })}
            className={Styles.button}
          >
            {t('dataDevelopment.definition.version.compareVersion')}{' '}
          </Button>
          <Table rowKey="id" rowSelection={rowSelection} columns={columns} dataSource={taskCommits} />
        </div>
      )}
    </div>
  );
});
