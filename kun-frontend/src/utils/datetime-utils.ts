import _dayjs from 'dayjs';
import dayjsUpdateLocalePlugin from 'dayjs/plugin/updateLocale';
import dayjsDurationPlugin from 'dayjs/plugin/duration';
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

export function dateFormatter(value: number | string) {
  const numberValue = Number(value);
  if (!value || Number.isNaN(numberValue)) {
    return '-';
  }
  return dayjs(numberValue).format('YYYY-MM-DD');
}
