import React, { useCallback, useEffect, useState } from 'react';
import c from 'classnames';
import { useRouteMatch } from 'umi';
import { Card, Form } from 'antd';
import { KunSpin } from '@/components/KunSpin';
import { useMount, useUnmount } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import { useTaskTemplateByName } from '@/hooks/useTaskTemplateByName';
import PollingLogViewer from '@/components/PollingLogViewer';

import { Header } from '@/pages/data-development/task-definition-config/components/Header';
import { BodyForm } from '@/pages/data-development/task-definition-config/components/BodyForm';
import { BottomLayout } from '@/pages/data-development/task-definition-config/components/BottomLayout';
import { dryRunTaskDefinition, fetchTaskTryLog } from '@/services/data-development/task-definitions';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import styles from './TaskDefinitionConfigView.less';

export const TaskDefinitionConfigView: React.FC<{}> = () => {
  const match = useRouteMatch<{ taskDefId: string; }>();
  const t = useI18n();
  const [ form ] = Form.useForm();
  const [ draftTaskDef, setDraftTaskDef ] = useState<TaskDefinition | null>(null);
  const [ taskTryId, setTaskTryId ] = useState<string | null>(null);

  const { selector: {
    initTaskDefinition,
  }, dispatch } = useRedux(s => ({
    initTaskDefinition: s.dataDevelopment.editingTaskDefinition,
  }));

  useMount(() => {
    if (match.params.taskDefId) {
      dispatch.dataDevelopment
        .fetchEditingTaskDefinition(match.params.taskDefId);
    }
  });

  useEffect(() => {
    setDraftTaskDef(initTaskDefinition);
  }, [
    initTaskDefinition,
    setDraftTaskDef,
  ]);

  const [
    taskTemplate,
    taskTemplateIsLoading
  ] = useTaskTemplateByName(initTaskDefinition?.taskTemplateName);

  useUnmount(() => {
    dispatch.dataDevelopment.setEditingTaskDefinition(null);
    dispatch.dataDevelopment.setDefinitionFormDirty(false);
  });

  const handleCommitDryRun = () => {
    const id = match.params.taskDefId;
    const runParameters = form.getFieldValue(['taskPayload', 'taskConfig']);
    dryRunTaskDefinition({
      taskDefId: id,
      parameters: runParameters,
      variables: {},
    }).then(vo => {
      if (vo) {
        setTaskTryId(vo.id);
      }
    });
  };

  const handleCloseDryRunLog = useCallback(() => {
    setTaskTryId(null);
  }, [
    setTaskTryId,
  ]);

  const logQueryFn = useCallback(() => {
    if (!taskTryId) {
      return Promise.resolve(null);
    }
    return fetchTaskTryLog(taskTryId || '');
  }, [taskTryId]);

  const bodyContent = draftTaskDef ? (
    <div className={styles.EditBody}>
      <Header
        draftTaskDef={draftTaskDef}
        setDraftTaskDef={setDraftTaskDef}
        form={form}
        taskDefId={match.params.taskDefId}
        handleCommitDryRun={handleCommitDryRun}
      />
      <main>
        {(() => {
          if (taskTemplateIsLoading) {
            return <KunSpin asBlock tip={t('common.loading')} />;
          }
          if (!taskTemplate) {
            return (
              <Card>
                Cannot load task template information. Please retry later
              </Card>
            );
          }
          // else
          return (
            <div>
              <BodyForm
                initTaskDefinition={initTaskDefinition || undefined}
                taskTemplate={taskTemplate}
                form={form}
              />
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

  return (
    <div className={c(styles.TaskDefinitionConfigView)}>
      {bodyContent}
      <BottomLayout
        visible={taskTryId !== null}
        title="Dry run logs"
        onClose={handleCloseDryRunLog}
      >
       <PollingLogViewer
         startPolling={taskTryId !== null}
         pollInterval={5000}  // poll log every 5 seconds
         queryFn={logQueryFn}
       />
      </BottomLayout>
    </div>
  );
};

export default TaskDefinitionConfigView;
