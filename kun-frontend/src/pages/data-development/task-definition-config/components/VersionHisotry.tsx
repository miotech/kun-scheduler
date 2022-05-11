import React, { useState, memo, useMemo } from 'react';
import useI18n from '@/hooks/useI18n';
import { Table, Button } from 'antd';
import { UsernameText } from '@/components/UsernameText';
import { TaskCommit } from '@/definitions/TaskDefinition.type';
import moment from 'moment';
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
  const [viewType, setViewType] = useState<string>('history');
  const [viewTaskCommit, setViewTaskCommit] = useState<TaskCommit | null>(null);

  const setView = (commit: TaskCommit) => {
    setViewType('viewVersion');
    setViewTaskCommit(commit);
  };
  const setDiff = (selecteds: TaskCommit[]) => {
    setViewType('diffVersion');
    setSelectRows(selecteds);
  };

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
            <span
              style={{ color: '#1A73E8', cursor: 'pointer' }}
              onClick={() => {
                setViewTaskCommit(record);
                setViewType('viewVersion');
              }}
            >
              {t('common.button.view')}
            </span>
          );
        },
      },
    ],
    [t],
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
      {viewType === 'viewVersion' && (
        <ViewVersion
          setView={(taskCommit: TaskCommit) => setView(taskCommit)}
          setDiff={(selecteds: TaskCommit[]) => setDiff(selecteds)}
          taskCommits={taskCommits}
          viewTaskCommit={viewTaskCommit}
          goBack={(type: string) => setViewType(type)}
        />
      )}
      {viewType === 'diffVersion' && (
        <DiffVersion
          setView={(taskCommit: TaskCommit) => setView(taskCommit)}
          diffRows={selectedRows}
          goBack={(type: string) => setViewType(type)}
        />
      )}
      {viewType === 'history' && (
        <div style={{ backgroundColor: '#fff', padding: '20px 30px' }}>
          <Button
            disabled={selectedRowKeys.length !== 2}
            type="primary"
            onClick={() => setViewType('diffVersion')}
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
