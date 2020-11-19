import React, { memo } from 'react';
import c from 'clsx';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { FunctionOutlined } from '@ant-design/icons';

import './TaskDAGNode.global.less';

interface OwnProps {
  /** Task definition data */
  taskDefinition: Partial<TaskDefinition>;
  /** allow interact with external inputs (mouse events, etc.)  */
  interoperable?: boolean;
  /** selection state */
  selected?: boolean;
}

type Props = OwnProps;

export const clsPrefix = 'task-dag-node';

export const TaskDAGNode: React.FC<Props> = memo(function TaskDAGNode(props) {
  const {
    taskDefinition,
    interoperable = true,
    selected = false,
  } = props;

  return (
    <div
      className={c(
        clsPrefix,
        {
          [`${clsPrefix}--interoperable`]: interoperable,
          [`${clsPrefix}--deployed`]: taskDefinition.isDeployed,
          [`${clsPrefix}--selected`]: selected,
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
