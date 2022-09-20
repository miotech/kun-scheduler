import React, { memo, useCallback } from 'react';
import { Modal } from 'antd';

import PollingLogViewer from '@/components/PollingLogViewer';
import { fetchTaskrunLog } from '@/services/task-tries/taskruns.service';
import { TaskRunLog } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';

import styles from './index.less';

interface Props {
  visible?: boolean;
  onClose?: () => any;
  taskRunId?: string | null;
}

const TaskRunLogViewer: React.FC<Props> = memo(props => {
  const { visible = false, onClose, taskRunId = null } = props;

  const t = useI18n();

  const onQuery = useCallback(() => {
    if (!taskRunId) {
      return Promise.resolve(null);
    }
    // else
    return fetchTaskrunLog(taskRunId).catch(e => {
      return {
        logs: [e.response?.data?.note],
      } as TaskRunLog;
    });
  }, [taskRunId]);

  const onDownload = useCallback(async () => {
    if (!taskRunId) {
      return Promise.resolve([]);
    }
    const payload = await fetchTaskrunLog(taskRunId, { start: 0, attempt: 0 });
    return payload?.logs || [];
  }, [taskRunId]);

  return (
    <Modal
      title={t('operationCenter.backfill.taskrun.operation.logs')}
      visible={visible}
      width={1200}
      destroyOnClose
      footer={null}
      onCancel={onClose}
      maskClosable={false}
    >
      <div className={styles.LogViewerWrapper}>
        <PollingLogViewer
          className={styles.LogViewer}
          startPolling={taskRunId != null}
          queryFn={onQuery}
          pollInterval={3000}
          presetLineLimit={5000}
          onDownload={onDownload}
          saveFileName={taskRunId ?? undefined}
        />
      </div>
    </Modal>
  );
});

export default TaskRunLogViewer;
