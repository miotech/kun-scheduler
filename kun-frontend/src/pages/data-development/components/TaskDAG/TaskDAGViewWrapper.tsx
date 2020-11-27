import React, { memo, useEffect, useState } from 'react';
import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';
import { fetchAllTaskDefinitions } from '@/services/data-development/task-definitions';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { fetchAllTaskDefinitionsByViewId } from '@/services/data-development/task-definition-views';
import { TaskDAG } from '@/pages/data-development/components/TaskDAG/TaskDAG';

interface OwnProps {
  taskDefViewId: string | null;
  filters: DataDevelopmentModelFilter;
  /** Triggers update when changed */
  updateTime: number;
  setSelectedTaskDefIds?: Function;
}

type Props = OwnProps;

export const TaskDAGViewWrapper: React.FC<Props> = memo(function TaskDAGViewWrapper(props) {
  const {
    taskDefViewId,
    // filters,
    // updateTime,
    // setSelectedTaskDefIds,
  } = props;

  const [ taskDefinitions, setTaskDefinitions ] = useState<TaskDefinition[]>([]);

  useEffect(() => {
    if (taskDefViewId == null) {
      fetchAllTaskDefinitions()
        .then(taskDefPayloads => {
          if (taskDefPayloads) {
            setTaskDefinitions(taskDefPayloads);
          }
        });
    } else {
      fetchAllTaskDefinitionsByViewId(taskDefViewId)
        .then(taskDefPayloads => {
          if (taskDefPayloads) {
            setTaskDefinitions(taskDefPayloads);
          }
        });
    }
  }, [
    taskDefViewId,
  ]);

  return (
    <TaskDAG
      taskDefinitions={taskDefinitions}
    />
  );
});
