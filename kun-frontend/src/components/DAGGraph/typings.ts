/**
 * Definitions for task DAG objects
 * @author Josh Ouyang
 */
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

export interface TaskNode {
  id: string;
  name: string;
  data?: TaskDefinition;
}

export interface TaskRelation {
  id: string;
  upstreamTaskId: string;
  downstreamTaskId: string;
}
