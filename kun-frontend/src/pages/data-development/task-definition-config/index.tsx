import React, { useCallback, useEffect, useMemo, useState } from 'react';
import c from 'clsx';
import { useRouteMatch } from 'umi';
import { Alert, Card, Form } from 'antd';
import { KunSpin } from '@/components/KunSpin';
import { useMount, useTitle, useUnmount } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import { useTaskTemplateByName } from '@/hooks/useTaskTemplateByName';
import PollingLogViewer from '@/components/PollingLogViewer';

import { Header } from '@/pages/data-development/task-definition-config/components/Header';
import { BodyForm } from '@/pages/data-development/task-definition-config/components/BodyForm';
import { BottomLayout } from '@/pages/data-development/task-definition-config/components/BottomLayout';
import {
  dryRunTaskDefinitionWithoutErrorNotification,
  fetchTaskTryLog,
  stopTaskTry,
} from '@/services/data-development/task-definitions';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { StatusText } from '@/components/StatusText';
import { usePrompt } from '@/hooks/usePrompt';
import { normalizeTaskDefinition, transformFormTaskConfig } from './helpers';

import styles from './TaskDefinitionConfigView.less';

export const TaskDefinitionConfigView: React.FC<{}> = function TaskDefinitionConfigView() {
  const match = useRouteMatch<{ taskDefId: string }>();
  const t = useI18n();
  const [form] = Form.useForm();
  const [draftTaskDef, setDraftTaskDef] = useState<TaskDefinition | null>(null);
  const [taskTryId, setTaskTryId] = useState<string | null>(null);
  const [taskTryStatus, setTaskTryStatus] = useState<RunStatusEnum>('CREATED');
  const [taskTryStopped, setTaskTryStopped] = useState<boolean>(true);
  const [alertMessage, setAlertMessage] = useState<string | null>(null);

  const {
    selector: { initTaskDefinition },
    dispatch,
  } = useRedux(s => ({
    initTaskDefinition: s.dataDevelopment.editingTaskDefinition,
  }));

  useTitle(
    initTaskDefinition != null
      ? `${t('common.pageTitle.taskDefinition')} - ${initTaskDefinition.name}`
      : t('common.pageTitle.taskDefinition'),
  );

  useMount(() => {
    if (match.params.taskDefId) {
      dispatch.dataDevelopment.fetchEditingTaskDefinition(match.params.taskDefId);
    }
  });

  const [taskTemplate, taskTemplateIsLoading] = useTaskTemplateByName(initTaskDefinition?.taskTemplateName);

  useEffect(() => {
    setDraftTaskDef(initTaskDefinition ? normalizeTaskDefinition(initTaskDefinition, taskTemplate || null) : null);
  }, [initTaskDefinition, taskTemplate]);

  useUnmount(() => {
    dispatch.dataDevelopment.setEditingTaskDefinition(null);
    dispatch.dataDevelopment.setDefinitionFormDirty(false);
  });

  const handleCommitDryRun = () => {
    const id = match.params.taskDefId;
    const runParameters = transformFormTaskConfig(form.getFieldValue(['taskPayload', 'taskConfig']), taskTemplate);
    dryRunTaskDefinitionWithoutErrorNotification({
      taskDefId: id,
      parameters: runParameters,
      variables: {},
    })
      .then(response => {
        if (response?.data?.result) {
          setTaskTryId(response?.data?.result.id);
        }
        setAlertMessage(null);
      })
      .catch(e => {
        setAlertMessage(e?.response?.data?.note || 'Unknown error occurred.');
      });
  };

  const handleCloseDryRunLog = useCallback(() => {
    setTaskTryId(null);
  }, [setTaskTryId]);

  const logQueryFn = useCallback(async () => {
    if (!taskTryId) {
      return Promise.resolve(null);
    }
    try {
      const response = await fetchTaskTryLog(taskTryId || '', { start: -5000 });
      if (response?.isTerminated) {
        setTaskTryStopped(true);
      }
      if (response?.status) {
        setTaskTryStatus(status => {
          if (
            status === 'ABORTING' &&
            response.status !== 'ABORTED' &&
            response.status !== 'SUCCESS' &&
            response.status !== 'FAILED'
          ) {
            return status;
          }
          // else
          return response.status;
        });
      }
      return response;
    } catch (e) {
      return Promise.reject(e);
    }
  }, [taskTryId]);

  useEffect(() => {
    if (taskTryId != null) {
      setTaskTryStatus('CREATED');
      setTaskTryStopped(false);
    }
  }, [taskTryId]);

  usePrompt(taskTryId != null && !taskTryStopped, t('dataDevelopment.dryRunIsStillRunningPromptText'));

  const bodyContent = draftTaskDef ? (
    <div className={styles.EditBody}>
      {alertMessage != null ? <Alert message={alertMessage} type="error" closable /> : <></>}
      <Header
        draftTaskDef={draftTaskDef}
        setDraftTaskDef={setDraftTaskDef}
        form={form}
        taskDefId={match.params.taskDefId}
        handleCommitDryRun={handleCommitDryRun}
        taskTemplate={taskTemplate}
      />
      <main>
        {(() => {
          if (taskTemplateIsLoading) {
            return <KunSpin asBlock tip={t('common.loading')} />;
          }
          if (!taskTemplate) {
            return <Card>Cannot load task template information. Please retry later</Card>;
          }
          // else
          return (
            <div>
              <BodyForm initTaskDefinition={draftTaskDef || undefined} taskTemplate={taskTemplate} form={form} />
            </div>
          );
        })()}
      </main>
    </div>
  ) : (
    <div className="">
      <KunSpin />
    </div>
  );

  const bottomLayoutTitle = useMemo(() => {
    return (
      <span style={{ fontSize: '14px' }}>
        <span>{t('dataDevelopment.dryRunLog')}</span>
        <span style={{ marginLeft: '8px' }}>
          <StatusText status={taskTryStatus} />
        </span>
      </span>
    );
  }, [t, taskTryId, taskTryStatus, taskTryStopped]);

  const handleStopDryRun = useCallback(
    function handleStopDryRun() {
      if (taskTryId != null) {
        setTaskTryStatus('ABORTING');
        setTaskTryStopped(true);
        stopTaskTry(taskTryId);
      }
    },
    [taskTryId],
  );

  return (
    <div className={c(styles.TaskDefinitionConfigView)}>
      {bodyContent}
      <BottomLayout
        visible={taskTryId !== null}
        title={bottomLayoutTitle}
        onStop={handleStopDryRun}
        onClose={handleCloseDryRunLog}
        stopBtnDisabled={taskTryStopped}
      >
        <PollingLogViewer
          startPolling={taskTryId !== null}
          pollInterval={5000} // poll log every 5 seconds
          queryFn={logQueryFn}
          saveFileName={taskTryId ?? undefined}
        />
      </BottomLayout>
    </div>
  );
};

export default TaskDefinitionConfigView;
