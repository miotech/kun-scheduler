import React, { useMemo, useState } from 'react';
import c from 'clsx';
import cronstrue from 'cronstrue/i18n';
import { Alert, Input } from 'antd';
import useI18n from '@/hooks/useI18n';
import { CronLocalization, ReQuartzCron } from '@sbzen/re-cron';

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

export const CronExpressionInput = React.forwardRef<Partial<HTMLInputElement>, CronExpressionInputProps>(
  function CronExpressionInput(props) {
    const { value, onChange, className, hideErrorAlert = false } = props;

    const t = useI18n();

    const [uncontrolledValue, setUncontrolledValue] = useState<string>('');

    const appliedValue = normalizeProvidedCronExpressionValue(value) ?? uncontrolledValue;

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
          value={appliedValue}
          onChange={ev => {
            if (onChange) {
              onChange(ev.target.value);
            } else {
              setUncontrolledValue(ev.target.value);
            }
          }}
        />
        <div className="cron-expression-input__semantic-wrapper">{semanticTip}</div>
        <div className={c('cron-expression-input', className)}>
          <ReQuartzCron
            value={appliedValue}
            onChange={(v: string) => {
              if (onChange) {
                onChange(v);
              } else {
                setUncontrolledValue(v);
              }
            }}
            localization={LOCALIZATION[t('common.lang')] || undefined}
          />
        </div>
      </div>
    );
  },
);
