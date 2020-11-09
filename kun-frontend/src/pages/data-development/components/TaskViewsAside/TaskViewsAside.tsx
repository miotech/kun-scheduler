import React, { memo } from 'react';

import styles from './TaskViewsAside.less';

interface OwnProps {}

type Props = OwnProps;

export const TaskViewsAside: React.FC<Props> = memo(function TaskViewsAside(props) {
  return (
    <aside data-tid="task-views-aside" className={styles.TaskViewsAside}>

    </aside>
  );
});
