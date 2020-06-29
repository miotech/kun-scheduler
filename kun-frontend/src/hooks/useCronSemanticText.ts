import cronstrue from 'cronstrue/i18n';
import useI18n from '@/hooks/useI18n';

export interface Options {
  throwExceptionOnParseError?: boolean;
  verbose?: boolean;
  dayOfWeekStartIndexZero?: boolean;
  use24HourTimeFormat?: boolean;
  locale?: string;
}

export default function useCronSemanticText(cronExpression: string, options: Partial<Options> = {}): string {
  const t = useI18n();
  const lang = t('common.cronstrue.lang');

  return cronstrue.toString(cronExpression, {
    locale: lang,
    use24HourTimeFormat: true,
    throwExceptionOnParseError: false,
    ...options,
  });
}
