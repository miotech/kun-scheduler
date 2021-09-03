import React, { memo, useMemo, useCallback } from 'react';
import { Checkbox } from 'antd';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import c from 'clsx';

import useI18n from '@/hooks/useI18n';

interface RetryCheckBoxProp {
  value?: number;
  onChange?: (nextValue: number) => any;
  className?: string;
}

export default memo(function RetryCheckBox({ value, onChange, className }: RetryCheckBoxProp) {
  const t = useI18n();

  const ckeckValue = useMemo(() => !!value, [value]);

  const handleChange = useCallback(
    (e: CheckboxChangeEvent) => {
      if (onChange) {
        if (e.target.checked) {
          onChange(1);
        } else {
          onChange(0);
        }
      }
    },
    [onChange],
  );

  return (
    <Checkbox className={c(className)} checked={ckeckValue} onChange={handleChange}>
      {t('dataDevelopment.definition.scheduleConfig.retryAfterFailed')}
    </Checkbox>
  );
});
