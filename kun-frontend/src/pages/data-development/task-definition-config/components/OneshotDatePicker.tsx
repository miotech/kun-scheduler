import React, { CSSProperties, memo, useCallback } from 'react';
import { DatePicker } from 'antd';
import moment, { Moment } from 'moment';
import { dateToOneShotCronExpression, parseDateFromOneShotCronExpression } from '@/utils/cronUtils';

interface OwnProps {
  value?: string;
  onChange?: (value: string) => any;
  style?: CSSProperties;
  className?: string;
  placeholder?: string;
}

type Props = OwnProps;

export const OneshotDatePicker: React.FC<Props> = memo(function OneshotDatePicker(props) {
  const { value, onChange, ...restProps } = props;

  const datePickerDisplayValue = parseDateFromOneShotCronExpression(value || '') ?
    moment(parseDateFromOneShotCronExpression(value || '')) : undefined;

  const wrappedOnChange = useCallback((m: Moment | null) => {
    if (!onChange) {
      return;
    }
    if (!m) {
      onChange('');
      return;
    }
    // else
    onChange(dateToOneShotCronExpression(m.toDate()));
  }, [
    onChange,
  ]);

  return (
    <DatePicker
      value={datePickerDisplayValue}
      onChange={wrappedOnChange as any}
      showTime={{
        format: 'HH:mm',
        showHour: true,
        showMinute: true,
        showSecond: false,
      }}
      format="YYYY-MM-DD HH:mm"
      {...restProps}
    />
  );
});
