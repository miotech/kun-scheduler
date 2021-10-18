import React, { FunctionComponent, useEffect } from 'react';
import { useRouteMatch, useHistory } from 'umi';
import { fetchDefinitionIdFromTaskRunId } from '@/services/data-development/task-definitions';

const TaskRunIdDirect: FunctionComponent<{}> = () => {
  const match = useRouteMatch<{ id: string }>();
  const history = useHistory();

  useEffect(() => {
    const func = async () => {
      const taskRunId = match.params.id;
      const definitionId = await fetchDefinitionIdFromTaskRunId(taskRunId);
      if (definitionId) {
        history.push(`/operation-center/scheduled-tasks/${definitionId}?taskRunId=${taskRunId}`);
      }
    };
    func();
  }, [history, match.params.id]);
  return <div />;
};

export default TaskRunIdDirect;
