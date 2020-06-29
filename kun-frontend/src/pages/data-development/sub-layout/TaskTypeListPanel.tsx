import React, { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { Button } from 'antd';
import { KunSpin } from '@/components/KunSpin';
import { ReloadOutlined } from '@ant-design/icons';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';

import useI18n from '@/hooks/useI18n';
import { TaskTypeList } from '@/pages/data-development/components/TaskTypeList';
import { TaskTypeItem } from '@/pages/data-development/components/TaskTypeItem';
import { fetchTaskTemplates } from '@/services/data-development/task-templates';

import styles from './TaskTypeListPanel.less';

export const TaskTypeListPanel: React.FC<any> = memo( () => {
  const [ loading, setLoading ] = useState<boolean>(false);
  const [ shouldRefresh, setShouldRefresh ] = useState<boolean>(true);
  const [ taskTemplates, setTaskTemplates ] = useState<TaskTemplate[]>([]);

  useEffect(() => {
    if (shouldRefresh) {
      setLoading(true);
      fetchTaskTemplates().then((templates) => {
        if (templates) {
          setTaskTemplates(templates);
        }
      }).finally(() => {
        setLoading(false);
        setShouldRefresh(false);
      });
    }
  }, [
    shouldRefresh,
    setLoading,
  ]);

  const t = useI18n();

  const handleReloadBtnClick = useCallback(() => {
    setShouldRefresh(true);
  }, [
    setShouldRefresh,
  ]);

  const taskTypeItemList = useMemo(() => {
    if (!taskTemplates || !taskTemplates.length) {
      return [];
    }
    // else
    return taskTemplates.map(taskTemplate =>
      <TaskTypeItem
        key={taskTemplate.name}
        taskTemplate={taskTemplate}
      />
    );
  }, [
    taskTemplates,
  ]);

  return (
    <section className={styles.TaskTypeListPanel}>
      <h2 className={styles.ListHeading}>
        {t('dataDevelopment.taskTypes')}
        <span className={styles.BtnRefreshWrapper}>
          <Button
            loading={loading}
            size="small"
            shape="circle"
            icon={<ReloadOutlined />}
            onClick={handleReloadBtnClick}
          />
        </span>
      </h2>
      <KunSpin spinning={loading} asBlock={!taskTypeItemList.length}>
        <TaskTypeList>
          {taskTypeItemList}
        </TaskTypeList>
      </KunSpin>
    </section>
  );
});
