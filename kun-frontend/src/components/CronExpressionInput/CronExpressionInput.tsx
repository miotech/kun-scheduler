import React, { MutableRefObject, useImperativeHandle, useMemo, useRef, useState } from 'react';
import c from 'clsx';
import cronstrue from 'cronstrue/i18n';
import { Alert, Input } from 'antd';
import useI18n from '@/hooks/useI18n';

import './CronExpressionInput.less';
import { validateQuartzCron } from '@/utils/cronUtils';

export interface CronExpressionInputProps {
  value?: string;
  onChange?: (value: string) => any;
  className?: string;
  hideErrorAlert?: boolean;
}

export const CronExpressionInput = React.forwardRef<Partial<HTMLInputElement>, CronExpressionInputProps>((props, ref) => {
  const {
    value,
    onChange,
    className,
    hideErrorAlert = false,
  } = props;

  const t = useI18n();

  const inputRef = useRef() as MutableRefObject<Input>;

  useImperativeHandle(ref, () => ({
    focus: () => {
      inputRef.current.focus();
    },
  }));

  const [ uncontrolledValue, setUncontrolledValue ] = useState<string>('');

  const appliedValue = value ?? uncontrolledValue;

  const semanticTip = useMemo(() => {
    if ((!appliedValue) || `${appliedValue}`.trim().length === 0) {
      return <></>;
    }
    let hasError: boolean;
    let semanticStr: string;
    try {
      semanticStr = cronstrue.toString(appliedValue, {
        throwExceptionOnParseError: true,
        locale: t('common.cronstrue.lang'),
      });
      hasError = (typeof value === 'string') ? (!validateQuartzCron(value)) : false;
    } catch (e) {
      semanticStr = '';
      hasError = true;
    }
    if (hasError && hideErrorAlert) {
      return <></>;
    }
    if (hasError) {
      return <Alert showIcon type="error" message={t('common.cronstrue.invalidCronExp')} />
    }
    // else
    return <Alert showIcon type="success" message={semanticStr} />
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    appliedValue,
    hideErrorAlert,
    t,
  ]);

  return (
    <div className="cron-expression-input-wrapper">
      <Input
        ref={inputRef}
        className={c('cron-expression-input', className)}
        value={appliedValue}
        onChange={(ev) => {
          if (onChange) {
            onChange(ev.target.value);
          } else {
            setUncontrolledValue(ev.target.value);
          }
        }}
      />
      <div className="cron-expression-input__semantic-wrapper">
        {semanticTip}
      </div>
    </div>
  );
});
