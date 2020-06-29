import React, { useEffect } from 'react';
import { useRequest } from 'ahooks';
import { fetchTaskDefinitionDetail } from '@/services/data-development/task-definitions';

export interface TaskDefinitionNameProps extends React.ComponentProps<'span'> {
  taskDefId?: string;
}

/**
 * A component fetches task definition name by id automatically
 */
export const TaskDefinitionName: React.FC<TaskDefinitionNameProps> = props => {
  const { taskDefId, ...restProps } = props;
  const { data, loading, run } = useRequest(fetchTaskDefinitionDetail, {
    manual: true,
    throttleInterval: 1000,
  });

  useEffect(() => {
    if (taskDefId && taskDefId !== 'undefined') {
      run(taskDefId);
    }
  }, [taskDefId]);

  return (
    <span data-label="task-definition-name" {...restProps}>
      {(loading || (!data)) ? '...' : data.name}
    </span>
  );
};
