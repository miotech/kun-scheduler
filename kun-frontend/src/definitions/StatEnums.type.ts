export type RunStatusEnum =
  | 'ABORTED'
  | 'ABORTING'
  | 'CREATED'
  | 'FAILED'
  | 'QUEUED'
  | 'RETRY'
  | 'RUNNING'
  | 'SKIPPED'
  | 'SUCCESS'
  | 'UPSTREAM_FAILED'
  | 'CHECK_FAILED';

export type DeployStatusEnum = 'CREATED' | 'FAILED' | 'SUCCESS' | 'WORKING';
