import React, { memo, useCallback, useMemo, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';

import { TaskViewListItem } from '@/pages/data-development/components/TaskViewsAside/TaskViewListItem';

import { TaskDefinitionViewBase, TaskDefinitionViewVO } from '@/definitions/TaskDefinitionView.type';

import { KunSpin } from '@/components/KunSpin';
import styles from './TaskViewsAside.module.less';

interface OwnProps {
  views: TaskDefinitionViewVO[];
  withAllTaskView?: boolean;
  onClickCreateBtn?: (ev: React.MouseEvent) => any;
  onEdit?: (view: TaskDefinitionViewVO) => any;
  onSearch?: (searchText: string) => any;
  loading?: boolean;
  onSelectItem?: (view: TaskDefinitionViewVO | null) => any;
  selectedView?: TaskDefinitionViewBase | null;
  allowAllTaskDefsView?: boolean;
}

type Props = OwnProps;

export const TaskViewsAside: React.FC<Props> = memo(function TaskViewsAside(props) {
  const {
    views = [],
    onEdit,
    onClickCreateBtn,
    onSearch,
    onSelectItem,
    loading,
    selectedView,
    allowAllTaskDefsView = true,
  } = props;

  const [ searchText, setSearchText ] = useState<string>('');

  const t = useI18n();

  const viewItems = useMemo(() => {
    let viewItemsDOMs = views.map(view => (
      <TaskViewListItem
        key={`${view.id}`}
        view={view}
        onEdit={onEdit}
        onSelect={onSelectItem}
        selected={(selectedView != null) ? view.id === selectedView.id : false}
      />
    ));
    if (allowAllTaskDefsView) {
      viewItemsDOMs = [
        <TaskViewListItem
          view={null}
          displayName="All Tasks"
          onSelect={onSelectItem}
          key="all-items"
          selected={selectedView == null}
        />,
        ...viewItemsDOMs,
      ];
    }
    return viewItemsDOMs;
  }, [
    allowAllTaskDefsView,
    onEdit,
    onSelectItem,
    selectedView,
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
      <KunSpin spinning={loading}>
        <ul className={styles.TaskViewsAsideList} data-tid="task-views-aside-list-wrapper">
          {viewItems}
        </ul>
      </KunSpin>
    </aside>
  );
});
