import { RunStatusEnum } from '@/definitions/StatEnums.type';

export const isStoppedStatus = (status: RunStatusEnum): boolean => {
  return (
    status === 'SKIPPED' || status === 'SUCCESS' || status === 'ABORTING' || status === 'ABORTED' || status === 'FAILED'
  );
};

export const isRunningStatus = (status: RunStatusEnum): boolean => {
  return status === 'RUNNING';
};
