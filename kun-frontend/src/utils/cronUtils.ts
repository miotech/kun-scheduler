import { cronix, CronixMode } from 'cronix';
import LogUtils from '@/utils/logUtils';

const logger = LogUtils.getLoggers('cronUtils');

export function validateQuartzCron(cronExpr: string): boolean {
  const parsedCronResult = cronix(cronExpr, CronixMode.QUARTZ);
  logger.debug('parsedCronResult =', parsedCronResult);
  if (parsedCronResult.errors && (parsedCronResult.errors.length > 0)) {
    return false;
  }
  // else
  return true;
}
