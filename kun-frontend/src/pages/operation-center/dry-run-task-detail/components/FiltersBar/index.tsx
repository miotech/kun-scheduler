import React, { useCallback, memo } from 'react';
import { Button, DatePicker } from 'antd';
import ReloadOutlined from '@ant-design/icons/lib/icons/ReloadOutlined';
import moment from 'moment';
import { Link } from 'umi';
import SafeUrlAssembler from 'safe-url-assembler';
import { ArrowLeftOutlined } from '@ant-design/icons';

import { StatusFilterSelect } from '@/components/StatusFilterSelect';
import useI18n from '@/hooks/useI18n';
import { RunStatusEnum } from '@/definitions/StatEnums.type';

import styles from './index.less';

export interface Filters {
  startTime?: string;
  endTime?: string;
  status?: RunStatusEnum;
}

interface Props {
  status?: RunStatusEnum;
  onFilterChange: (filters: Filters) => void;
  onRefresh: () => void;
  loading: boolean;
  taskDefId: string;
}

const FilterBar: React.FC<Props> = memo(({ status, onRefresh, loading, onFilterChange, taskDefId }) => {
  const t = useI18n();

  const handleStatusChange = useCallback(
    (value: RunStatusEnum) => {
      onFilterChange({ status: value });
    },
    [onFilterChange],
  );

  const handleDateChange = useCallback(
    dates => {
      const [startTime, endTime] = dates || [];
      onFilterChange({
        startTime: startTime && moment(startTime).toISOString(),
        endTime: endTime && moment(endTime).toISOString(),
      });
    },
    [onFilterChange],
  );

  return (
    <div className={styles.FilterBar}>
      <div>
        <StatusFilterSelect
          className={styles.StatusSel}
          disabled={loading}
          value={status}
          onChange={handleStatusChange}
          allowClear
        />
        <DatePicker.RangePicker
          onChange={handleDateChange}
          ranges={{
            [t('dataDiscovery.mode.quickOption.LAST_1_D')]: [moment().startOf('day'), moment().endOf('day')],
            [t('dataDiscovery.mode.quickOption.LAST_1_W')]: [moment().startOf('week'), moment().endOf('week')],
            [t('dataDiscovery.mode.quickOption.LAST_1_MON')]: [moment().startOf('month'), moment().endOf('month')],
          }}
          allowEmpty={[true, true]}
          allowClear
          showNow
          showTime
          format="YYYY/MM/DD HH:mm:ss"
        />
      </div>
      <div>
        <Link
          to={SafeUrlAssembler()
            .template('/data-development/task-definition/:taskDefId')
            .param({
              taskDefId,
            })
            .toString()}
        >
          <Button icon={<ArrowLeftOutlined />}>{t('scheduledTasks.jumpToTaskDef')}</Button>
        </Link>
        <Button className={styles.RefreshBtn} loading={loading} icon={<ReloadOutlined />} onClick={onRefresh}>
          {t('common.refresh')}
        </Button>
      </div>
    </div>
  );
});

export default FilterBar;
