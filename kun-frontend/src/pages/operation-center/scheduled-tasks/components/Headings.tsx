import React, { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Input, Select, Space } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';
import { fetchTaskTemplates } from '@/services/data-development/task-templates';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';

import styles from './Headings.less';
import useRedux from '@/hooks/useRedux';

interface HeadingsProps {
}

export const Headings: React.FC<HeadingsProps> = memo(() => {
  const t = useI18n();

  const {
    selector: {
      searchName,
      taskTemplateName,
      loading,
    },
    dispatch,
  } = useRedux(s => ({
    searchName: s.scheduledTasks.filters.searchName,
    taskTemplateName: s.scheduledTasks.filters.taskTemplateName,
    loading: s.loading.effects.scheduledTasks.fetchScheduledTasks,
  }));

  const [taskTemplates, setTaskTemplates] = useState<TaskTemplate[]>([]);

  useEffect(() => {
    fetchTaskTemplates()
      .then(templates => setTaskTemplates(templates || []))
      .catch(() => {
        setTaskTemplates([]);
      });
  }, []);

  const taskTemplateFilterOptions = useMemo(() => {
    if (taskTemplates && taskTemplates.length) {
      return taskTemplates.map(template => (
        <Select.Option key={template.name} value={template.name}>
          {template.name}
        </Select.Option>
      ));
    }
    // else
    return [];
  }, [taskTemplates]);

  const handleSearchChange = useCallback((ev) => {
    dispatch.scheduledTasks.updateFilter({
      searchName: ev.target.value,
    });
  }, [
    dispatch,
  ]);

  const handleTaskTemplateTypeFilterChange = useCallback((nextValue: string) => {
    dispatch.scheduledTasks.updateFilter({
      taskTemplateName: nextValue || '',
    });
  }, [
    dispatch,
  ]);

  return (
    <nav className={styles.Headings}>
      <div className={styles.FiltersGroup}>
        <Space>
          {/* Search input */}
          <Input.Search
            style={{ width: '310px' }}
            value={searchName}
            onChange={handleSearchChange}
          />
          {/* Task template type filter */}
          <Select
            style={{ width: '180px' }}
            showSearch
            allowClear
            placeholder={t('scheduledTasks.filters.taskType')}
            value={taskTemplateName || undefined}
            onChange={handleTaskTemplateTypeFilterChange}
          >
            {taskTemplateFilterOptions}
          </Select>
        </Space>
      </div>
      <div className={styles.RefreshBtnWrapper}>
        <Button
          loading={loading}
          icon={<ReloadOutlined />}
          onClick={() => { dispatch.scheduledTasks.setShouldRefresh(true); }}
        >
          {t('common.refresh')}
        </Button>
      </div>
    </nav>
  );
});
