import React, { memo, useMemo } from 'react';
import { TaskTemplateIcon } from '@/components/TaskTemplateIcon/TaskTemplateIcon.component';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { adjustColor, hexToRgbA, operatorNameToHexColor } from '@/components/Workflow/helpers/operatorNameToHexColor';

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

export const TaskDAGNodeSimplify: React.FC<Props> = memo(function TaskDAGNodeSimplify(props) {
  const {
    taskDefinition,
  } = props;

  const color = useMemo(() => operatorNameToHexColor(taskDefinition.taskTemplateName || ''), [taskDefinition.taskTemplateName]);

  return (
    <div className="task-dag-node-simplify" style={{ borderColor: color }} data-tid="task-dag-node-simplify">
      <div className="task-dag-node-simplify__icon-wrapper" style={{ borderColor: color, background: hexToRgbA(adjustColor(color, 40), 0.2) }} data-tid="dag-node-left">
        <span style={{ color }}>
          <TaskTemplateIcon
            className="task-dag-node-simplify__icon"
            name={taskDefinition.taskTemplateName}
          />
        </span>
      </div>
      <div className="task-dag-node-simplify__info" data-tid="dag-node-right">
        <div
          className="task-dag-node-simplify__type"
          style={{ color }}
        >
          {taskDefinition.taskTemplateName}
        </div>
        <div className="task-dag-node-simplify__name">
          {taskDefinition.name}
        </div>
      </div>
    </div>
  );
});
