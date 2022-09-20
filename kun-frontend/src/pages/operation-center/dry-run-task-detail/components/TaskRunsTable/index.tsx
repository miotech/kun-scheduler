import React, { useCallback, useState } from 'react';
import { Table } from 'antd';
import { useRequest } from 'ahooks';

import { stopTaskRunInstance, restartTaskRunInstance } from '@/services/task-tries/taskruns.service';

import TaskRunLogViewer from '../TaskRunLogViewer';

import { useColumns } from './helper';

interface Props {
  onPaginationChange: (pageNum: number, pageSize: number) => void;
  pageNum: number;
  pageSize: number;
  data: any[];
  total: number;
  loading: boolean;
  onTaskStop: () => void;
  onTaskRerun: () => void;
}

const TaskRunsTable: React.FC<Props> = React.memo(
  ({ onPaginationChange, pageNum, pageSize, total, loading, data, onTaskRerun, onTaskStop }) => {
    const [taskRunId, setTaskRunId] = useState(null);
    const { run: stopTask } = useRequest(stopTaskRunInstance, { manual: true, onSuccess: onTaskStop });
    const { run: rerunTask } = useRequest(restartTaskRunInstance, { manual: true, onSuccess: onTaskRerun });
    const viewLog = useCallback(id => {
      setTaskRunId(id);
    }, []);

    const handleLogViewerClose = useCallback(() => {
      setTaskRunId(null);
    }, []);

    const columns = useColumns(stopTask, rerunTask, viewLog);

    return (
      <div>
        <Table
          bordered
          loading={loading}
          dataSource={data}
          columns={columns}
          pagination={{
            total,
            pageSize,
            current: pageNum,
            onChange: onPaginationChange,
            showSizeChanger: false,
          }}
        />
        <TaskRunLogViewer visible={!!taskRunId} onClose={handleLogViewerClose} taskRunId={taskRunId} />
      </div>
    );
  },
);

export default TaskRunsTable;
