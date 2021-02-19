import React, { memo, useCallback } from 'react';
import { Modal } from 'antd';
import PollingLogViewer from '@/components/PollingLogViewer';
import { fetchScheduledTaskRunLogWithoutErrorNotification } from '@/services/task-deployments/deployed-tasks';
import { TaskRunLog } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';

import css from './BackfillInstanceLogViewer.less';

interface OwnProps {
  visible?: boolean;
  onClose?: () => any;
  taskRunId?: string | null;
}

type Props = OwnProps;

export const BackfillInstanceLogViewer: React.FC<Props> = memo(
  function BackfillInstanceLogViewer(props) {
    const { visible = false, onClose, taskRunId = null } = props;

    const t = useI18n();

    const onQuery = useCallback(() => {
      if (!taskRunId) {
        return Promise.resolve(null);
      }
      // else
      return fetchScheduledTaskRunLogWithoutErrorNotification(taskRunId).catch(
        e => {
          return {
            logs: [e.response?.data?.note],
          } as TaskRunLog;
        },
      );
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
        <div className={css.LogViewerWrapper}>
          <PollingLogViewer
            className={css.LogViewer}
            startPolling={taskRunId != null}
            queryFn={onQuery}
            pollInterval={2000}
            saveFileName={taskRunId ?? undefined}
          />
        </div>
      </Modal>
    );
  },
);
