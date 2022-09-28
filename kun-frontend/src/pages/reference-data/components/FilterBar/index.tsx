import React, { useMemo, useCallback } from 'react';
import { Input, Select, DatePicker } from 'antd';
import { RangePickerProps } from 'antd/es/date-picker';
import moment, { Moment } from 'moment';
import useRequest from 'ahooks/es/useRequest';

import useI18n from '@/hooks/useI18n';
import { FetchRdmDatasParams } from '@/definitions/ReferenceData.type';
import { queryRdmAttributeList } from '@/services/reference-data/referenceData';
import styles from './index.less';

export interface Filters extends Omit<FetchRdmDatasParams, 'pageSize' | 'pageNum'> {}

interface Props {
  filters: Filters;
  totalCount: number;
  onFilterChange: (filters: Filters) => void;
}

type TimeRange = [Moment | null, Moment | null];

const fetchGlossaryByOwners = async (owners?: string): Promise<Record<string, string>[]> => {
  const glossaryOptions = await queryRdmAttributeList({
    resourceAttributeName: 'glossaries',
    resourceAttributeMap: { ...(owners ? { owners } : {}) },
  });
  return glossaryOptions.map((glossary: string) => JSON.parse(glossary));
};

const fetchOwnersByGlossary = async (glossaries?: Record<string, any>[]): Promise<string[]> => {
  return queryRdmAttributeList({
    resourceAttributeName: 'owners',
    resourceAttributeMap: {
      ...(Array.isArray(glossaries) ? { glossaries } : {}),
    },
  });
};

const { Search } = Input;
const { Option } = Select;

const RangePicker: React.FC<RangePickerProps> = ({ value, onChange }) => {
  const t = useI18n();
  return (
    <DatePicker.RangePicker
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
      value={value}
      onChange={onChange}
    />
  );
};

const convertTimeStrToMoment = (time?: string): TimeRange[0] => {
  return time ? moment(time) : null;
};

const formatTimeRange = (startTime: string | undefined, endTime: string | undefined): TimeRange => {
  return [convertTimeStrToMoment(startTime), convertTimeStrToMoment(endTime)];
};

const formatMomentTime = (time?: Moment | null): undefined | string => {
  return time ? moment(time).toISOString() : undefined;
};

const generateTimeFilter = (timeRange: TimeRange | undefined, suffix: 'CreateTime' | 'UpdateTime') => {
  const [startTime, endTime] = timeRange?.map(time => formatMomentTime(time)) || [];
  return {
    [`start${suffix}`]: startTime,
    [`end${suffix}`]: endTime,
  };
};

const useTimeRange = (filters: Filters): Record<'createTime' | 'updateTime', TimeRange> => {
  const { startCreateTime, endCreateTime, startUpdateTime, endUpdateTime } = filters;
  const createTime = useMemo(() => {
    return formatTimeRange(startCreateTime, endCreateTime);
  }, [startCreateTime, endCreateTime]);
  const updateTime = useMemo(() => formatTimeRange(startUpdateTime, endUpdateTime), [startUpdateTime, endUpdateTime]);
  return {
    createTime,
    updateTime,
  };
};

const FilterBar: React.FC<Props> = ({ filters, totalCount, onFilterChange }) => {
  const t = useI18n();
  // Fetch glossary list
  const { data: glossaryOptions } = useRequest(fetchGlossaryByOwners.bind(null, filters.owners), {
    refreshDeps: [filters.owners],
  });

  // Fetch owner list
  const { data: ownerOptions } = useRequest(fetchOwnersByGlossary.bind(null, filters.glossaries), {
    refreshDeps: [filters.glossaries],
  });

  const { createTime, updateTime } = useTimeRange(filters);

  const handleFilterChange = useCallback(
    (
      filterName: 'createTime' | 'updateTime' | 'keyword' | 'owners' | 'glossaries',
      value?: string | TimeRange | null,
    ) => {
      let updatedFilters: Partial<Filters> = {};
      switch (filterName) {
        case 'createTime':
          updatedFilters = generateTimeFilter(value as TimeRange, 'CreateTime');
          break;
        case 'updateTime':
          updatedFilters = generateTimeFilter(value as TimeRange, 'UpdateTime');
          break;
        case 'glossaries':
          updatedFilters = {
            [filterName]: value ? [JSON.parse(value as string)] : undefined,
          };
          break;
        default:
          updatedFilters = {
            [filterName]: value,
          };
      }
      onFilterChange(updatedFilters);
    },
    [onFilterChange],
  );

  return (
    <div className={styles.Container}>
      <div className={styles.SearchInput}>
        <Search
          onChange={event => handleFilterChange('keyword', event.target.value)}
          value={filters.keyword}
          size="large"
          placeholder={t('dataDiscovery.searchContent')}
          style={{ width: '70%' }}
        />
      </div>
      <div className={styles.TotalCount}>{t('common.table.resultCount', { count: totalCount })}</div>
      <div className={styles.SelectionGroup}>
        <div className={styles.FilterItem}>
          <div className={styles.Label}>{t('dataDiscovery.referenceData.filter.glossary')}</div>
          <Select
            allowClear
            onChange={(value: string) => handleFilterChange('glossaries', value)}
            style={{ width: '224px' }}
            placeholder={t('common.pleaseSelect')}
          >
            {glossaryOptions?.map(glossary => {
              const [key, label] = Object.entries(glossary)?.[0] || [];
              return (
                <Option key={key} value={JSON.stringify({ [key]: label })}>
                  {label}
                </Option>
              );
            })}
          </Select>
        </div>
        <div className={styles.FilterItem}>
          <div className={styles.Label}>{t('dataDiscovery.referenceData.filter.owners')}</div>
          <Select
            allowClear
            onChange={(value: string) => handleFilterChange('owners', value)}
            style={{ width: '224px' }}
            placeholder={t('common.pleaseSelect')}
          >
            {ownerOptions?.map(owner => (
              <Option key={owner} value={owner}>
                {owner}
              </Option>
            ))}
          </Select>
        </div>
        <div className={styles.FilterItem}>
          <div className={styles.Label}>{t('dataDiscovery.referenceData.filter.createTime')}</div>
          <RangePicker value={createTime} onChange={value => handleFilterChange('createTime', value as TimeRange)} />
        </div>
        <div className={styles.FilterItem}>
          <div className={styles.Label}>{t('dataDiscovery.referenceData.filter.updateTime')}</div>
          <RangePicker value={updateTime} onChange={value => handleFilterChange('updateTime', value as TimeRange)} />
        </div>
      </div>
    </div>
  );
};

export { FilterBar };
