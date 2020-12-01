import uniqueId from 'lodash/uniqueId';
import isArray from 'lodash';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskNode, TaskRelation } from '@/components/DAGGraph/typings';
import LogUtils from '@/utils/logUtils';

export interface TaskGraphData {
  nodes: TaskNode[];
  relations: TaskRelation[];
}

const logger = LogUtils.getLoggers('taskDefsToGraph');

export function taskDefsToGraph(taskDefs: TaskDefinition[]): TaskGraphData {
  if (!isArray(taskDefs)) {
    logger.warn('Invalid argument taskDefs: %o', taskDefs);
    return {
      nodes: [],
      relations: [],
    };
  }

  const nodes: TaskNode[] = [];
  const relations: TaskRelation[] = [];

  taskDefs.forEach(taskDef => {
    // insert node
    nodes.push({
      id: `${taskDef.id}`,
      name: `${taskDef.name}`,
      data: taskDef,
    });
    // insert relations
    if (taskDef.upstreamTaskDefinitions?.length > 0) {
      taskDef.upstreamTaskDefinitions.forEach(upstreamTask => {
         relations.push({
          downstreamTaskId: `${taskDef.id}`,
          id: uniqueId(),
          upstreamTaskId: `${upstreamTask.id}`,
        });
      });
    }
  });

  return {
    nodes,
    relations,
  };
}
