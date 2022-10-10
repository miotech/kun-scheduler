import _dayjs from 'dayjs';
import dayjsUpdateLocalePlugin from 'dayjs/plugin/updateLocale';
import dayjsDurationPlugin, { Duration } from 'dayjs/plugin/duration';
import dayjsRelativeTimePlugin from 'dayjs/plugin/relativeTime';
import dayjsRelativeTimeLocaleZhCN from '@/constants/dayjs-relative-time-locale.zh-cn';
import locale_zh_CN from 'dayjs/locale/zh-cn';

// @ts-ignore
if (!window.dayjs) {
  _dayjs.extend(dayjsUpdateLocalePlugin);
  _dayjs.extend(dayjsDurationPlugin);
  _dayjs.extend(dayjsRelativeTimePlugin);

  const lang = document.documentElement.getAttribute('lang');
  if (lang === 'zh-CN') {
    _dayjs.locale('zh-CN', locale_zh_CN);
    _dayjs.updateLocale('zh-CN', dayjsRelativeTimeLocaleZhCN);
  }

  // @ts-ignore
  window.dayjs = _dayjs;
}

// @ts-ignore
export const dayjs = window.dayjs as typeof _dayjs;

export function dateFormatter(value: number | string | undefined) {
  if (!value) {
    return '-';
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm');
}

/**
 * //FIXME: support mutiple format
 * This util fomarts dayjs duration object to HH:mm:ss' format
 * @param duration
 * @returns
 */
export const durationFormatter = (duration: Duration) => {
  const formattedSequence: Array<{
    unit: keyof Pick<Duration, 'hours' | 'minutes' | 'seconds'>;
    joiner: '-' | ':' | '' | ' ';
  }> = [
    { unit: 'hours', joiner: ':' },
    { unit: 'minutes', joiner: ':' },
    { unit: 'seconds', joiner: '' },
  ];
  return formattedSequence.reduce((res: string, { unit, joiner }: typeof formattedSequence[0]) => {
    const value = duration[unit]();
    return `${res}${value < 0 ? 0 : value}${joiner}`;
  }, '');
};
