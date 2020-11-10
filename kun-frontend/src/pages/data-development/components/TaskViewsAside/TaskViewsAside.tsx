import React, { memo, useMemo } from 'react';

import { TaskViewListItem } from '@/pages/data-development/components/TaskViewsAside/TaskViewListItem';

import { TaskDefinitionView } from '@/definitions/TaskDefinition.type';

import '@/global.less';
import styles from './TaskViewsAside.module.less';

interface OwnProps {
  views: TaskDefinitionView[];
  withAllTaskView?: boolean;
}

type Props = OwnProps;

export const TaskViewsAside: React.FC<Props> = memo(function TaskViewsAside(props) {
  const {
    views = [],
  } = props;

  const viewItems = useMemo(() => views.map(view => (
    <TaskViewListItem
      key={`${view.id}`}
      view={view}
    />
  )), [
    views,
  ]);

  return (
    <aside data-tid="task-views-aside" className={styles.TaskViewsAside}>
      <ul className={styles.TaskViewsAsideList} data-tid="task-views-aside-list-wrapper">
        {viewItems}
      </ul>
    </aside>
  );
});
