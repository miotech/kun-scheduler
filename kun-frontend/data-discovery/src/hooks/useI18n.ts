import { useCallback } from 'react';
import { useIntl } from 'umi';

export default function useI18n() {
  const intl = useIntl();
  const myIntl = useCallback(
    (key: string, options?: any, defaultMsg: string | undefined = '') =>
      intl.formatMessage(
        {
          id: key,
          defaultMessage: defaultMsg,
        },
        options,
      ),
    [intl],
  );
  return myIntl;
}
