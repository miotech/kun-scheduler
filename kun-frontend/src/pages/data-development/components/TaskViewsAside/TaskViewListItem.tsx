import React, { memo, useCallback, useMemo } from 'react';
import c from 'clsx';
import { EditOutlined } from '@ant-design/icons';

import { TaskDefinitionViewVO } from '@/definitions/TaskDefinitionView.type';

import styles from './TaskViewsAside.module.less';

interface OwnProps {
  view: TaskDefinitionViewVO | null;
  displayName?: string;
  onEdit?: (view: TaskDefinitionViewVO) => any;
  onSelect?: (view: TaskDefinitionViewVO | null) => any;
  selected?: boolean;
}

type Props = OwnProps;

export const TaskViewListItem: React.FC<Props> = memo(function TaskViewItem(props) {
  const { view, onEdit, onSelect, selected, displayName } = props;

  const handleSelectClick = useCallback((ev: any) => {
    ev.preventDefault();
    if (onSelect) {
      onSelect(view);
    }
  }, [
    onSelect,
    view,
  ]);

  const editBtn = useMemo(() => {
    if (view != null) {
      return (
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
      );
    }
    // else
    return null;
  }, [
    onEdit,
    view,
  ]);

  return (
    <li
      className={c(styles.TaskViewListItem, {
        [styles.TaskViewListItemSelected]: selected,
      })}
      data-tid="task-view-list-item"
    >
      <button
        type="button"
        className={styles.TaskViewListItemInnerBtn}
        data-tid="task-view-list-item-button"
        title={displayName || view?.name}
        onClick={handleSelectClick}
      >
        <span
          className={styles.ViewName}
          data-tid="task-view-list-item-button__view-name"
        >
          {displayName || view?.name}
        </span>
      </button>
      <span
        className={styles.TaskDefCount}
        data-tid="task-view-list-item-button__task-def-count"
      >
        {(view?.includedTaskDefinitionIds || []).length}
      </span>
      {editBtn}
    </li>
  );
});
