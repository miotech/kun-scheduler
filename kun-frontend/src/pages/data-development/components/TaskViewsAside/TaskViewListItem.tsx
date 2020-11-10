import React, { memo } from 'react';
import { EditOutlined } from '@ant-design/icons';

import { TaskDefinitionView } from '@/definitions/TaskDefinition.type';

import styles from './TaskViewsAside.module.less';

interface OwnProps {
  view: TaskDefinitionView;
  onEdit?: (view: TaskDefinitionView) => any;
}

type Props = OwnProps;

export const TaskViewListItem: React.FC<Props> = memo(function TaskViewItem(props) {
  const { view, onEdit } = props;

  return (
    <li className={styles.TaskViewListItem} data-tid="task-view-list-item">
      <button
        type="button"
        className={styles.TaskViewListItemInnerBtn}
        data-tid="task-view-list-item-button"
        title={view.name}
      >
        <span
          className={styles.ViewName}
          data-tid="task-view-list-item-button__view-name"
        >
          {view.name}
        </span>
      </button>
      <span
        className={styles.TaskDefCount}
        data-tid="task-view-list-item-button__task-def-count"
      >
        {(view.taskDefIds || []).length}
      </span>
      <span
        className={styles.EditBtnWrapper}
        data-tid="task-view-list-item-button__edit-button-wrapper"
      >
        <button
          type="button"
          className={styles.EditBtn}
          data-tid="task-view-list-item-button__edit-button"
          onClick={function onClickHandler(ev) {
            ev.stopPropagation();
            if (onEdit) {
              onEdit(view);
            }
          }}
        >
          <EditOutlined />
        </button>
      </span>
    </li>
  );
});
