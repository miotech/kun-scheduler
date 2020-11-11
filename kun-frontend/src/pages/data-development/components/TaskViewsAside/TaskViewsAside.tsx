import React, { memo, useCallback, useMemo, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';

import { TaskViewListItem } from '@/pages/data-development/components/TaskViewsAside/TaskViewListItem';

import { TaskDefinitionView } from '@/definitions/TaskDefinition.type';

import styles from './TaskViewsAside.module.less';

interface OwnProps {
  views: TaskDefinitionView[];
  withAllTaskView?: boolean;
  onClickCreateBtn?: (ev: React.MouseEvent) => any;
  onEdit?: (view: TaskDefinitionView) => any;
  onSearch?: (searchText: string) => any;
}

type Props = OwnProps;

export const TaskViewsAside: React.FC<Props> = memo(function TaskViewsAside(props) {
  const {
    views = [],
    onEdit,
    onClickCreateBtn,
    onSearch,
  } = props;

  const [ searchText, setSearchText ] = useState<string>('');

  const t = useI18n();

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

  const handleViewSearch = useCallback(function handleViewSearch(ev) {
    setSearchText(ev.target.value);
    if (onSearch) {
      onSearch(ev.target.value);
    }
  }, [
    onSearch,
  ]);

  return (
    <aside data-tid="task-views-aside" className={styles.TaskViewsAside}>
      <div data-tid="task-views-aside-header" className={styles.AsideHeader}>
        <div className={styles.SearchWrapper}>
          <input
            placeholder={t('dataDevelopment.searchView')}
            className={styles.BorderlessSearchInput}
            value={searchText}
            onChange={handleViewSearch}
          />
        </div>
        <div className={styles.CreateViewBtnWrapper}>
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
      </div>
      <ul className={styles.TaskViewsAsideList} data-tid="task-views-aside-list-wrapper">
        {viewItems}
      </ul>
    </aside>
  );
});
