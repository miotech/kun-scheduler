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
import LogUtils from '@/utils/logUtils';
import { doSQLExecute } from '@/services/code-hint/sql-dry-run';
import { SQLQueryTab } from '@/definitions/QueryResult.type';
import { SqlDryRunBottomLayout } from '@/pages/data-development/task-definition-config/components/SqlDryRunBottomLayout';
import { normalizeTaskDefinition, transformFormTaskConfig } from './helpers';

import styles from './TaskDefinitionConfigView.less';

const logger = LogUtils.getLoggers('TaskDefinitionConfigView');

export const TaskDefinitionConfigView: React.FC<{}> = function TaskDefinitionConfigView() {
  const match = useRouteMatch<{ taskDefId: string }>();
  const t = useI18n();
  const [form] = Form.useForm();
  const [draftTaskDef, setDraftTaskDef] = useState<TaskDefinition | null>(null);
  const [taskTryId, setTaskTryId] = useState<string | null>(null);
  const [taskTryStatus, setTaskTryStatus] = useState<RunStatusEnum>('CREATED');
  const [taskTryStopped, setTaskTryStopped] = useState<boolean>(true);
  const [alertMessage, setAlertMessage] = useState<string | null>(null);
  const [sqlDryRunTabs, setSqlDryRunTabs] = useState<SQLQueryTab[] | null>(null);
  const [sqlDryRunOrdinal, setSqlDryRunOrdinal] = useState<number>(1);

  const {
    selector: { initTaskDefinition, formIsDirty },
    dispatch,
  } = useRedux(s => ({
    initTaskDefinition: s.dataDevelopment.editingTaskDefinition,
    formIsDirty: s.dataDevelopment.definitionFormDirty,
  }));

  /* Is it a SQL task? If true, then dry run should display table of SQL query results instead of run logs */
  const isSQLTask: boolean = useMemo(() => {
    // TODO: reconsider how to implement this after we figure out how to run SQL code line-by-line with thrift server.
    return false;
    /*
    if (initTaskDefinition?.taskTemplateName === 'SparkSQL') {
      return true;
    }
    // else
    return false;
    */
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    initTaskDefinition,
  ]);

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

  const doSqlExecuteAndFetchPage = useCallback(async (sql: string, pageNum: number, pageSize: number) => {
    return doSQLExecute({
      sql,
      pageNum,
      pageSize,
    });
  }, []);

  const handleCommitSQLDryRun = function handleCommitSQLDryRun() {
    try {
      const monacoModels = window.monaco.editor.getModels();
      if (monacoModels.length > 0) {
        // TODO: handle multiple editor model cases
        const monacoModel = monacoModels[0];
        // logger.debug('Model = ', monacoModel);
        // logger.debug('Value = ', monacoModel.getValue());
        const sqlValue: string = monacoModel.getValue();
        if (sqlValue && sqlValue.length) {
          const nextSqlDrayRunTabs = (sqlDryRunTabs == null) ? [] : [...sqlDryRunTabs];
          const id = `${Date.now()}`;
          doSQLExecute({
            sql: sqlValue,
            pageNum: 1,
            pageSize: 100,
          }).then(data => {
            setSqlDryRunTabs(lastState => {
              const newState = [...(lastState || [])];
              const targetTabIdx = newState.findIndex(tab => tab.id === id);
              newState[targetTabIdx].response = data;
              newState[targetTabIdx].done = true;
              return newState;
            });
          }).catch(() => {
            setSqlDryRunTabs(lastState => {
              const newState = [...(lastState || [])];
              const targetTabIdx = newState.findIndex(tab => tab.id === id);
              newState[targetTabIdx].done = true;
              return newState;
            });
          });
          nextSqlDrayRunTabs.push({
            response: null,
            id,
            done: false,
            ordinal: sqlDryRunOrdinal,
            sql: sqlValue,
            pageNum: 1,
            pageSize: 100,
          });
          setSqlDryRunTabs(nextSqlDrayRunTabs);
          setSqlDryRunOrdinal(sqlDryRunOrdinal + 1);
        }
      }
    } catch (e) {
      logger.error('Cannot find monaco editor instance');
    }
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
  usePrompt(formIsDirty);

  const bodyContent = draftTaskDef ? (
    <div className={styles.EditBody}>
      {alertMessage != null ? <Alert message={alertMessage} type="error" closable /> : <></>}
      <Header
        draftTaskDef={draftTaskDef}
        setDraftTaskDef={setDraftTaskDef}
        form={form}
        taskDefId={match.params.taskDefId}
        handleCommitDryRun={isSQLTask ? handleCommitSQLDryRun : handleCommitDryRun}
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
  }, [t, taskTryStatus]);

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
      {isSQLTask ? <></> : (
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
      )}
      {(isSQLTask && sqlDryRunTabs?.length) ?
        <SqlDryRunBottomLayout
          tabs={sqlDryRunTabs}
          setTabs={setSqlDryRunTabs}
          doSqlExecuteAndFetchPage={doSqlExecuteAndFetchPage}
        /> : <></>}
    </div>
  );
};

export default TaskDefinitionConfigView;
