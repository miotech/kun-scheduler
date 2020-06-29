import React, { FunctionComponent, useCallback } from 'react';
import c from 'classnames';
import { Link } from 'umi';
import { Space, DatePicker, Button } from 'antd';
import moment, { Moment } from 'moment';
import { LeftOutlined, ReloadOutlined } from '@ant-design/icons';

import useI18n from '@/hooks/useI18n';
import { StatusFilterSelect } from '@/components/StatusFilterSelect';

import { TaskRunListFilter } from '@/rematch/models/operationCenter/deployedTaskDetail/model-state';
import { RootDispatch } from '@/rematch/store';
import { RunStatusEnum } from '@/definitions/StatEnums.type';

import styles from './TaskRunsFilterBar.less';

export interface TaskRunsFilterBarProps {
  filter: TaskRunListFilter;
  dispatch: RootDispatch;
  onClickRefresh: () => void;
}

const TaskRunsFilterBar: FunctionComponent<TaskRunsFilterBarProps> = (props) => {
  const t = useI18n();

  const { filter, dispatch, onClickRefresh } = props;

  const handleTimeRangeFilterChange = useCallback((dates?: [Moment, Moment]) => {
    dispatch.deployedTaskDetail.updateFilter({
      startTime: dates?.[0] || null,
      endTime: dates?.[1] || null,
    });
  }, [
    dispatch
  ]);

  return (
    <nav id="taskruns-filter-bar" className={styles.TaskRunsFilterBar}>
      <Space>
        <span className={styles.FilterItemBlock}>
          <Link to="/operation-center/scheduled-tasks">
            <span>
              <LeftOutlined />
            </span>
            <span>{t('scheduledTasks.backToTaskList')}</span>
          </Link>
        </span>
        {/* Status filter selector */}
        <span className={styles.FilterItemBlock}>
          <StatusFilterSelect
            style={{ width: '120px', marginLeft: '30px' }}
            allowClear
            value={filter.status || undefined}
            placeholder={t('scheduledTasks.property.stat')}
            onChange={(nextFilterValue: RunStatusEnum) => {
              dispatch.deployedTaskDetail.updateFilter({
                status: nextFilterValue || null,
              });
            }}
          />
        </span>
        {/* Time range selector */}
        <span className={styles.FilterItemBlock}>
          <DatePicker.RangePicker
            ranges={{
              [t('dataDiscovery.mode.quickOption.LAST_1_D')]: [
                moment().startOf('day'),
                moment().endOf('day'),
              ],
              [t('dataDiscovery.mode.quickOption.LAST_1_W')]: [
                moment().startOf('week'),
                moment().endOf('week'),
              ],
              [t('dataDiscovery.mode.quickOption.LAST_1_MON')]: [
                moment().startOf('month'),
                moment().endOf('month'),
              ],
            }}
            allowEmpty={[ true, true ]}
            allowClear
            showNow
            showTime
            format="YYYY/MM/DD HH:mm:ss"
            // @ts-ignore
            onChange={handleTimeRangeFilterChange}
          />
        </span>
      </Space>
      <span className={c(styles.FilterItemBlock, styles.RefreshBtnWrapper)}>
        <Button
          icon={<ReloadOutlined />}
          onClick={onClickRefresh}
        >
          {t('common.refresh')}
        </Button>
      </span>
    </nav>
  );
};

export default TaskRunsFilterBar;
