export type RunStatusEnum =
  | 'ABORTED'
  | 'ABORTING'
  | 'CREATED'
  | 'FAILED'
  | 'QUEUED'
  | 'RETRY'
  | 'RUNNING'
  | 'SKIPPED'
  | 'SUCCESS';

export type DeployStatusEnum =
  | 'CREATED'
  | 'FAILED'
  | 'SUCCESS'
  | 'WORKING';
