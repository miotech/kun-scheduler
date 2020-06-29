import React, { useCallback } from 'react';
import { Button } from 'antd';
import { EditOutlined, AimOutlined } from '@ant-design/icons';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import styles from './TaskDefinitionList.less';

export interface TaskDefinitionItemProps {
  taskDefinition: TaskDefinition;
  onClick?: (taskDef: TaskDefinition) => any;
  onClickCenter?: (taskDef: TaskDefinition) => any;
  onClickEdit?: (taskDef: TaskDefinition) => any;
}

export const TaskDefinitionItem: React.FC<TaskDefinitionItemProps> = props => {
  const {
    taskDefinition,
    onClick,
    onClickCenter,
  } = props;

  const handleClick = useCallback(() => {
    onClick && onClick(taskDefinition);
  }, [
    onClick,
  ]);

  const handleClickCenter = useCallback(() => {
    if (onClickCenter) {
      onClickCenter(taskDefinition);
    }
  }, [
    taskDefinition,
    onClickCenter,
  ]);

  return (
    <li className={styles.TaskDefinitionItem}>
      <span className={styles.DefItemInnerWrapper}>
        <Button
          type="link"
          className={styles.TaskDefLinkBtn}
          onClick={handleClick}
        >
          {taskDefinition.name}
        </Button>
        <span className={styles.ButtonGroup}>
          <Button
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              if (props.onClickEdit) {
                props.onClickEdit(taskDefinition);
              }
            }}
          />
          <Button
            size="small"
            icon={<AimOutlined />}
            onClick={() => {
              handleClickCenter();
            }}
          />
        </span>
      </span>
    </li>
  );
};
