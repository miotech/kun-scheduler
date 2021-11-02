import React, { useMemo, useState } from 'react';
import c from 'clsx';
import cronstrue from 'cronstrue/i18n';
import { Alert, Input } from 'antd';
import useI18n from '@/hooks/useI18n';
import { CronLocalization, ReQuartzCron, Tab } from '@sbzen/re-cron';
import { validate } from '@joshoy/quartz-cron-parser';

import ReCronZhCNLocalization from './i18n/recron.zh-cn';
import './CronExpressionInput.less';

const LOCALIZATION: Record<string, CronLocalization> = {
  'zh-CN': ReCronZhCNLocalization,
};

export interface CronExpressionInputProps {
  value?: string;
  onChange?: (value: string) => any;
  className?: string;
  hideErrorAlert?: boolean;
}

function normalizeProvidedCronExpressionValue(value?: string): string | undefined {
  if (!value) {
    return undefined;
  }
  // else
  return value
    .trim()
    .split(' ')
    .filter(part => part.length > 0)
    .join(' ');
}

function fixScopValue(value?: string) {
  if (!value) {
    return '';
  }

  const strArr = value.split(' ');
  const newArr = [...strArr];
  strArr.forEach((i, idx) => {
    if (i.includes('-')) {
      const arr = i.split('-');
      const start = Number(arr[0]);
      const end = Number(arr[1]);
      if (start > end) {
        newArr[idx] = `${start}-${start}`;
      } else {
        newArr[idx] = `${start}-${end}`;
      }
    }
  });

  const res = newArr.join(' ');
  return res;
}

export const CronExpressionInput = React.forwardRef<Partial<HTMLInputElement>, CronExpressionInputProps>(
  function CronExpressionInput(props) {
    const { value, onChange, className, hideErrorAlert = false } = props;

    const t = useI18n();

    const [uncontrolledValue, setUncontrolledValue] = useState<string>('');

    const appliedValue = normalizeProvidedCronExpressionValue(value) ?? uncontrolledValue;

    const isValidCronExpressionFlag = validate(`${appliedValue}`);

    const semanticTip = useMemo(() => {
      if (!appliedValue) {
        return <></>;
      }
      let hasError: boolean;
      let semanticStr: string;
      try {
        semanticStr = cronstrue.toString(appliedValue, {
          throwExceptionOnParseError: true,
          locale: t('common.cronstrue.lang'),
        });
        hasError = false;
      } catch (e) {
        semanticStr = '';
        hasError = true;
      }
      if (hasError && hideErrorAlert) {
        return <></>;
      }
      if (hasError) {
        return <Alert showIcon type="error" message={t('common.cronstrue.invalidCronExp')} />;
      }
      // else
      return <Alert showIcon type="success" message={semanticStr} />;
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [appliedValue, hideErrorAlert, t]);

    return (
      <div className="cron-expression-input-wrapper">
        <Input
          value={value}
          onChange={ev => {
            if (onChange) {
              onChange(ev.target.value);
            } else {
              setUncontrolledValue(ev.target.value);
            }
          }}
        />
        <div className="cron-expression-input__selector-component-wrapper" />
        {isValidCronExpressionFlag ? (
          <>
            {semanticTip}
            <div className={c('cron-expression-input', className)}>
              <ReQuartzCron
                value={appliedValue}
                tabs={[Tab.MINUTES, Tab.HOURS, Tab.DAY, Tab.MONTH, Tab.YEAR]}
                onChange={(v: string) => {
                  const v1 = fixScopValue(v);
                  if (onChange) {
                    onChange(v1);
                  } else {
                    setUncontrolledValue(v1);
                  }
                }}
                localization={LOCALIZATION[t('common.lang')] || undefined}
              />
            </div>
          </>
        ) : (
          <></>
        )}
      </div>
    );
  },
);
