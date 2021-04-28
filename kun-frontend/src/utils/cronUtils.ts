import { cronix, CronixMode } from 'cronix';
import isNaN from 'lodash/isNaN';
import dayjs from 'dayjs';
import LogUtils from '@/utils/logUtils';

const logger = LogUtils.getLoggers('cronUtils');

/**
 * Validate a quartz cron expression
 * @param cronExpr cron expression in string
 * @return true if valid, else false
 */
export function validateQuartzCron(cronExpr: string): boolean {
  if (
    cronExpr.match(/[1-5]?[0-9] [1-5]?[0-9] (1?[0-9]|20|21|22|23) ([1-9]|[1-2][0-9]|30|31) ([1-9]|11|12) \? [0-9]+/)
  ) {
    return true;
  }
  const parsedCronResult = cronix(cronExpr, CronixMode.QUARTZ);
  logger.info('parsedCronResult =', parsedCronResult);
  if (parsedCronResult.errors && parsedCronResult.errors.length > 0) {
    return false;
  }
  // else
  return true;
}

export interface ParseOptions {
  ignoreSeconds?: boolean;
  utcOffsetHours?: number;
}

/**
 * Parse date to a single-shot cron expression in quartz format
 * @param date
 * @param options
 */
export function dateToOneShotCronExpression(
  date: Date | number,
  options: ParseOptions = { ignoreSeconds: true },
): string {
  // eslint-disable-next-line no-bitwise
  const offsetHours = (options.utcOffsetHours ?? getSystemDefaultOffsetHours()) | 0;
  const { ignoreSeconds = true } = options;
  const momentObj = dayjs(date)
    .add(offsetHours, 'hour')
    .subtract(dayjs(date).utcOffset(), 'minute');

  if (ignoreSeconds) {
    return `0 ${momentObj.minute()} ${momentObj.hour()} ${momentObj.date()} ${momentObj.month() +
      1} ? ${momentObj.year()}`;
  }
  // else
  return `${momentObj.second()} ${momentObj.minute()} ${momentObj.hour()} ${momentObj.date()} ${momentObj.month() +
    1} ? ${momentObj.year()}`;
}

function getSystemDefaultOffsetHours(): number {
  return -(new Date().getTimezoneOffset() / 60);
}

export function parseDateFromOneShotCronExpression(cronExpr: string): Date | undefined {
  if (!cronExpr) {
    return undefined;
  }
  const matches = cronExpr.trim().match(/^([1-5]?[0-9]) (\d+) (\d+) (\d+) (\d+) \? (\d+)$/);
  logger.debug('matches =', matches);
  if (!matches) {
    return undefined;
  }
  const date = new Date(`${matches[6]}-${matches[5]}-${matches[4]} ${matches[3]}:${matches[2]}:${matches[1]}`);
  if (isNaN(date.getDate())) {
    return undefined;
  }
  return date;
}
