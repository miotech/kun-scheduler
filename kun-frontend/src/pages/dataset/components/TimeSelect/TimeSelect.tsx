import React, { memo, useMemo, useCallback } from 'react';
import c from 'classnames';
import { Select, DatePicker } from 'antd';
import { SwapRightOutlined } from '@ant-design/icons';
import moment from 'moment';
import useI18n from '@/hooks/useI18n';
import { DataRange, Quick, Mode } from '@/rematch/models/dataDiscovery';

import styles from './TimeSelect.less';

const { Option } = Select;

interface Props {
  style?: React.CSSProperties;
  className?: string;
  mode: Mode;
  onModeChange: (value: Mode) => void;
  onChange: (value: DataRange | Quick | null, mode: Mode) => void;
  value?: DataRange | Quick | null;
}

export default memo(function TimeSelect({
  style,
  className,
  mode,
  onChange,
  onModeChange,
  value,
}: Props) {
  const t = useI18n();

  const handleChangeStartTime = useCallback(
    (v: moment.Moment | null) => {
      let endTime = null;
      if (value) {
        ({ endTime } = value as DataRange);
      }
      if (endTime || v) {
        onChange(
          { startTime: v ? v.valueOf() : null, endTime: Number(endTime) },
          Mode.ABSOLUTE,
        );
      } else {
        onChange(null, Mode.ABSOLUTE);
      }
    },
    [onChange, value],
  );

  const handleChangeEndTime = useCallback(
    (v: moment.Moment | null) => {
      let startTime = null;
      if (value) {
        ({ startTime } = value as DataRange);
      }
      if (startTime || v) {
        onChange(
          { endTime: v ? v.valueOf() : null, startTime: Number(startTime) },
          Mode.ABSOLUTE,
        );
      } else {
        onChange(null, Mode.ABSOLUTE);
      }
    },
    [onChange, value],
  );

  const handleChangeQuickSelect = useCallback(
    (v: Quick) => {
      onChange(v, Mode.QUICK);
    },
    [onChange],
  );

  const timeComp = useMemo(() => {
    if (mode === Mode.ABSOLUTE) {
      let startTime = null;
      let endTime = null;
      if (value) {
        ({ startTime, endTime } = value as DataRange);
      }
      return (
        // <RangePicker
        //   size='large'
        //   className={styles.rangePicker}
        //   bordered={false}
        //   value={rangeValue as any}
        //   onChange={handleChangeRangePicker as any}
        // />
        <div className={styles.rangePicker}>
          <DatePicker
            size="large"
            className={styles.dataPicker}
            bordered={false}
            showTime={{ defaultValue: moment().startOf('day') }}
            value={startTime ? moment(Number(startTime)) : null}
            suffixIcon={null}
            onChange={handleChangeStartTime as any}
            placeholder={t('dataDiscovery.datapicker.please.start')}
          />
          <SwapRightOutlined />
          <DatePicker
            size="large"
            className={styles.dataPicker}
            bordered={false}
            showTime={{ defaultValue: moment().endOf('day') }}
            value={endTime ? moment(Number(endTime)) : null}
            onChange={handleChangeEndTime as any}
            placeholder={t('dataDiscovery.datapicker.please.end')}
          />
        </div>
      );
    }
    return (
      <Select
        className={styles.quickSelector}
        size="large"
        value={(value as Quick) || undefined}
        onChange={handleChangeQuickSelect}
        bordered={false}
        allowClear
      >
        {Object.values(Quick).map(quickItem => (
          <Option key={quickItem} value={quickItem}>
            {t(`dataDiscovery.mode.quickOption.${quickItem}`)}
          </Option>
        ))}
      </Select>
    );
  }, [
    mode,
    value,
    handleChangeQuickSelect,
    handleChangeStartTime,
    handleChangeEndTime,
    t,
  ]);

  return (
    <div className={c(styles.timeSelect, className)} style={style}>
      <Select
        className={styles.modeSelect}
        size="large"
        value={mode}
        onChange={onModeChange}
        bordered={false}
      >
        {Object.values(Mode).map((modeItem: Mode) => (
          <Option key={modeItem} value={modeItem}>
            {t(`dataDiscovery.mode.${modeItem}`)}
          </Option>
        ))}
      </Select>
      <div className={styles.seporater} />
      {timeComp}
    </div>
  );
});
