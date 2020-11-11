import React, { memo } from 'react';
import c from 'clsx';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { FunctionOutlined } from '@ant-design/icons';

import './TaskDAGGraph.less';

export const TASK_DAG_NODE_WIDTH = 240;
export const TASK_DAG_NODE_HEIGHT = 60;

interface OwnProps {
  /** Task definition data */
  taskDefinition: TaskDefinition;
  /** allow interact with external inputs (mouse events, etc.)  */
  interoperable?: boolean;
}

type Props = OwnProps;

export const clsPrefix = 'task-dag-node';

export const TaskDAGNode: React.FC<Props> = memo(function TaskDAGNode(props) {
  const {
    taskDefinition,
    interoperable = true,
  } = props;

  return (
    <div
      className={c(
        clsPrefix,
        {
          [`${clsPrefix}--interoperable`]: interoperable,
          [`${clsPrefix}--deployed`]: taskDefinition.isDeployed,
        },
      )}
      data-tid="task-dag-node"
    >
      <div
        className={`${clsPrefix}__heading`}
        data-tid="task-dag-node-heading"
      >
        <div
          className={`${clsPrefix}__heading__metas`}
          data-tid="task-dag-node-metas"
        >
          <span
            className={`${clsPrefix}__heading__icon`}
            data-tid="task-dag-node-icon"
          >
            <FunctionOutlined />
          </span>
          <span
            className={`${clsPrefix}__heading__task-template-name`}
            data-tid="task-template-name"
          >
            {taskDefinition.taskTemplateName}
          </span>
        </div>
      </div>
      <div
        className={`${clsPrefix}__center`}
        data-tid="task-dag-node-center"
      >
        <h2
          className={`${clsPrefix}__task-def-name`}
          data-tid="task-dag-node-name"
          title={interoperable ? taskDefinition.name : undefined}
        >
          {taskDefinition.name}
        </h2>
      </div>
      <div
        className={`${clsPrefix}__bottom`}
        data-tid="task-dag-node-bottom"
       />
    </div>
  );
});
