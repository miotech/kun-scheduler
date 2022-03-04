import React, { memo, SyntheticEvent, useCallback, useState } from 'react';
import { DatePicker, Button, Select } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { Filters, TaskState } from '@/definitions/Gantt.type';
import useI18n from '@/hooks/useI18n';

import moment, { Moment } from 'moment';
import css from './ViewFilters.less';

interface OwnProps {
  filters: Filters;
  task: TaskState | undefined;
  setTask: (task?: TaskState) => void;
  updateFilters: (params: Filters) => void;
  onClickRefresh: (ev: SyntheticEvent) => any;
  refreshBtnLoading?: boolean;
}

type Props = OwnProps;

const { Option } = Select;

export const ViewFilters: React.FC<Props> = memo(function ViewFilters(props) {
  const t = useI18n();

  const { updateFilters, onClickRefresh, refreshBtnLoading = false, task, setTask } = props;
  const [dates, setDates] = useState([]);
  const disabledDate = useCallback(
    (current: any) => {
      if (!dates || dates.length === 0) {
        return false;
      }
      const tooLate = dates[0] && current.diff(dates[0], 'days') > 7;
      const tooEarly = dates[1] && dates[1].diff(current, 'days') > 7;
      return tooEarly || tooLate;
    },
    [dates],
  );

  const handleTimeRangeFilterChange = useCallback(
    (values?: [Moment, Moment]) => {
      if (values) {
        updateFilters({
          startTime: moment(values?.[0]).toISOString() || null,
          endTime: moment(values?.[1]).toISOString() || null,
        });
      } else {
        updateFilters({
          startTime: null,
          endTime: null,
        });
      }
    },
    [updateFilters],
  );
  const handleChange = useCallback(
    timeType => {
      updateFilters({
        timeType,
      });
    },
    [updateFilters],
  );

  return (
    <nav className={css.ViewFilters}>
      <div className={css.filterOption}>
        {/* type  of time */}
        <div className={css.timeTypeWrapper}>
          <Select defaultValue="createdAt" style={{ width: 100 }} onChange={handleChange}>
            <Option value="createdAt">created-at</Option>
            <Option value="queuedAt">queued-at</Option>
            <Option value="startAt">start-at</Option>
            <Option value="endAt">end-at</Option>
          </Select>
        </div>
        {/* running  time range */}
        <div className={css.RangePickerWrapper}>
          <DatePicker.RangePicker
            ranges={{
              [t('dataDiscovery.mode.quickOption.LAST_1_D')]: [
                moment()
                  .add(-1, 'days')
                  .startOf('day'),
                moment()
                  .add(-1, 'days')
                  .endOf('day'),
              ],
              [t('dataDiscovery.mode.quickOption.LAST_1_W')]: [
                moment()
                  .add(-7, 'days')
                  .startOf('day'),
                moment()
                  .add(-1, 'days')
                  .endOf('day'),
              ],
            }}
            allowEmpty={[true, true]}
            allowClear
            showNow
            showTime
            format="YYYY/MM/DD HH:mm:ss"
            disabledDate={disabledDate}
            onCalendarChange={val => setDates(val)}
            // @ts-ignore
            onChange={handleTimeRangeFilterChange}
          />
        </div>
        {task?.taskName && (
          <div className={css.taskWrapper}>
            <Select
              defaultValue={task && task.taskName}
              style={{ width: 180 }}
              onClear={() => setTask()}
              placeholder="上下游关系的任务名称"
              allowClear
            />
          </div>
        )}
      </div>
      <div className={css.right}>
        <div className={css.tips}>
          <div className={css.tip}>{t('operationCenter.runningStatistics.waitTime')}</div>
          <div className={css.tip}>{t('operationCenter.runningStatistics.runningTimeSuc')}</div>
          <div className={css.tip}>{t('operationCenter.runningStatistics.runningTimeFail')}</div>
          <div className={css.tip}>{t('operationCenter.runningStatistics.averageRunningTime')}</div>
        </div>
        <div className={css.ReloadBtnWrapper}>
          <Button icon={<ReloadOutlined />} onClick={onClickRefresh} loading={refreshBtnLoading}>
            {t('common.refresh')}
          </Button>
        </div>
      </div>
    </nav>
  );
});
