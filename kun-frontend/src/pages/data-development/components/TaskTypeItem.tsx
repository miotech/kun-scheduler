import React, { useMemo } from 'react';
import { DragSourceMonitor, useDrag } from 'react-dnd';
import { Tooltip } from 'antd';
import { SettingOutlined } from '@ant-design/icons';
import useRedux from '@/hooks/useRedux';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';

import styles from './TaskType.less';

export interface TaskTypeItemProps {
  showTooltip?: boolean;
  taskTemplate: TaskTemplate;
}

export const TaskTypeItem: React.FC<TaskTypeItemProps> = props => {
  const { showTooltip, taskTemplate } = props;

  const { dispatch } = useRedux(() => ({}));

  const [{ isDragging }, drag] = useDrag({
    item: {
      type: 'TaskTemplate',
      ...taskTemplate,
    },
    options: {
      dropEffect: 'copy',
    },
    end: (item: any, monitor: DragSourceMonitor) => {
      const dropResult = monitor.getDropResult();
      if (item && dropResult) {
        if (dropResult.droppedContainer === 'GraphPanel') {
          /* Set current creating task type and call up modal */
          dispatch.dataDevelopment.setCreatingTaskTemplate(item);
        }
      }
    },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  });

  const opacity = isDragging ? 0.4 : 1;

  const elementBodyContent = (
    <div className={styles.ElementInner}>
        <span className={styles.TaskIconWrapper}>
          <SettingOutlined />
        </span>
      <span className={styles.TaskNameWrapper}>
        {taskTemplate?.name}
      </span>
    </div>
  );

  const displayContent = useMemo(() => {
    if (isDragging || (typeof showTooltip === 'boolean' && !showTooltip)) {
      return elementBodyContent;
    }
    // else
    return (
      <Tooltip title={taskTemplate?.name || ''} placement="right">
        {elementBodyContent}
      </Tooltip>
    );
  }, [
    taskTemplate,
    showTooltip,
    elementBodyContent,
    isDragging,
  ]);

  return (
    <li ref={drag} className={styles.TaskTypeElement} style={{ opacity }}>
      {displayContent}
    </li>
  )
};
