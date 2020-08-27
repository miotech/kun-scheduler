import React from 'react';
import c from 'classnames';
// import { LogUtils } from '@/utils/logUtils';

import { TaskTypeItemProps } from './TaskTypeItem';
import styles from './TaskType.less';

export interface TaskTypeListProps extends React.ComponentProps<any> {
  children: React.ReactElement<TaskTypeItemProps> | React.ReactElement<TaskTypeItemProps>[];
}

// const logger = LogUtils.getLoggers('TaskTypeList');

export const TaskTypeList: React.FC<TaskTypeListProps> = props => {
  const { className, ...restProps } = props;
  return (
    <ul
      className={c(styles.TaskTypeList, className)}
      {...restProps}
    >
      {props.children}
    </ul>
  );
};
