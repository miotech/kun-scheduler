import React, { ChangeEventHandler, memo, useCallback, useMemo, useState } from 'react';
import { useMount } from 'ahooks';
import { Col, Input, Row, Select } from 'antd';
import useI18n from '@/hooks/useI18n';
import { connect } from 'react-redux';
import { fetchTaskTemplates } from '@/services/data-development/task-templates';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { RootDispatch, RootState } from '@/rematch/store';
import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';

import { UserSelect } from '@/components/UserSelect';
import { DisplayTypeSwitch } from '@/pages/data-development/components/DisplayTypeSwitch/DisplayTypeSwitch';
import styles from './TaskDefinitionFilterToolbar.module.less';

interface OwnProps {
  displayType: 'DAG' | 'LIST';
  dispatch: RootDispatch;
  filters: DataDevelopmentModelFilter;
}

type Props = OwnProps;

export const TaskDefinitionFilterToolbarComponent: React.FC<Props> = memo((props) => {
  const {
    dispatch,
    filters,
    displayType,
  } = props;
  const t = useI18n();
  const [ taskTemplates, setTaskTemplates ] = useState<TaskTemplate[]>([]);

  useMount(() => {
    fetchTaskTemplates().then((templates) => {
      if (templates) {
        setTaskTemplates(templates);
      }
    });
  });

  const taskTemplateFilterOptions = useMemo(() => {
    return taskTemplates.map(taskTemplate => (
      <Select.Option key={taskTemplate.name} value={taskTemplate.name}>
        {taskTemplate.name}
      </Select.Option>
    ));
  }, [
    taskTemplates
  ]);

  const handleSearchNameChange: ChangeEventHandler<any> = useCallback((ev) => {
    dispatch.dataDevelopment.updateFilter({
      name: ev.target.value,
    });
  }, [
    dispatch,
  ]);

  const handleTaskTemplateNameChange = useCallback((value?: string) => {
    dispatch.dataDevelopment.updateFilter({
      taskTemplateName: value || null,
    });
  }, [
    dispatch,
  ]);

  const handleOwnerChange = useCallback((value?: string | string[]) => {
    if (typeof value === 'string') {
      return;
    }
    dispatch.dataDevelopment.updateFilter({
      creatorIds: value || [],
    });
  }, [
    dispatch,
  ]);

  return (
    <div
      data-tid="task-definition-filter-toolbar"
      className={styles.Toolbar}
    >
      <div className={styles.ToolbarLeftGroup}>
        <DisplayTypeSwitch
          currentType={displayType}
          onChange={(nextType) => {
            dispatch.dataDevelopment.setDisplayType(nextType);
          }}
        />
      </div>
      <div className={styles.ToolbarRightGroup}>
        <Row>
          <Col className="gutter-row" span={8}>
            <Input.Search
              value={filters.name}
              onChange={handleSearchNameChange}
              className="full-width"
              placeholder={t('dataDevelopment.filterByName')}
            />
          </Col>
          <Col className="gutter-row" span={8}>
            <Select
              placeholder={t('dataDevelopment.filterByTaskType')}
              className="full-width"
              allowClear
              value={filters.taskTemplateName || undefined}
              onChange={handleTaskTemplateNameChange}
            >
              {taskTemplateFilterOptions}
            </Select>
          </Col>
          <Col className="gutter-row" span={8}>
            <UserSelect
              mode="multiple"
              value={filters.creatorIds}
              onChange={handleOwnerChange}
              allowClear
              placeholder={t('dataDevelopment.filterByOwner')}
              className="full-width"
            />
          </Col>
        </Row>
      </div>
    </div>
  );
});

const mapStateToProps = (state: RootState) => ({
  filters: state.dataDevelopment.filters,
  displayType: state.dataDevelopment.displayType,
});

const mapDispatchToProps = (dispatch: any) => ({
  dispatch,
});

export const TaskDefinitionFilterToolbar = connect(
  mapStateToProps,
  mapDispatchToProps,
)(TaskDefinitionFilterToolbarComponent);

TaskDefinitionFilterToolbar.displayName = 'TaskDefinitionFilterToolbar';
