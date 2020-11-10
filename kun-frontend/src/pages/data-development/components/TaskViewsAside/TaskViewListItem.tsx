import React, { memo } from 'react';

import styles from './TaskViewsAside.less';
import { TaskDefinitionView } from '@/definitions/TaskDefinition.type';

interface OwnProps {
  view: TaskDefinitionView;
}

type Props = OwnProps;

export const TaskViewListItem: React.FC<Props> = memo(function TaskViewItem(props) {
  return (
    <li className={styles.TaskViewListItem} data-tid="task-view-list-item">
      <button
        type="button"
        className={styles.TaskViewListItemInnerBtn}
        data-tid="task-view-list-item-button"
      >

      </button>
    </li>
  );
});
