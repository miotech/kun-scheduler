import React, { memo, useMemo } from 'react';
import { PlusOutlined } from '@ant-design/icons';

import { TaskViewListItem } from '@/pages/data-development/components/TaskViewsAside/TaskViewListItem';

import { TaskDefinitionView } from '@/definitions/TaskDefinition.type';

import styles from './TaskViewsAside.module.less';

interface OwnProps {
  views: TaskDefinitionView[];
  withAllTaskView?: boolean;
  onClickCreateBtn?: (ev: React.MouseEvent) => any;
  onEdit?: (view: TaskDefinitionView) => any;
}

type Props = OwnProps;

export const TaskViewsAside: React.FC<Props> = memo(function TaskViewsAside(props) {
  const {
    views = [],
    onEdit,
    onClickCreateBtn,
  } = props;

  const viewItems = useMemo(() => views.map(view => (
    <TaskViewListItem
      key={`${view.id}`}
      view={view}
      onEdit={onEdit}
    />
  )), [
    onEdit,
    views,
  ]);

  return (
    <aside data-tid="task-views-aside" className={styles.TaskViewsAside}>
      <div data-tid="task-views-aside-header" className={styles.AsideHeader}>
        <h2>Select View</h2>
        <button
          className={styles.CreateViewBtn}
          type="button"
          data-tid="task-views-create-btn"
          aria-label="create new task view"
          onClick={onClickCreateBtn}
        >
          <PlusOutlined />
        </button>
      </div>
      <ul className={styles.TaskViewsAsideList} data-tid="task-views-aside-list-wrapper">
        {viewItems}
      </ul>
    </aside>
  );
});
