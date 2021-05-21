import React, { FunctionComponent } from 'react';
import { Select } from 'antd';
import { SelectProps } from 'antd/es/select';
import { RunStatusEnum } from '@/definitions/StatEnums.type';

export interface StatusFilterSelectProps extends SelectProps<RunStatusEnum> {}

const runStatusList: RunStatusEnum[] = [
  'ABORTED',
  'ABORTING',
  'CREATED',
  'FAILED',
  'QUEUED',
  'RETRY',
  'RUNNING',
  'SKIPPED',
  'SUCCESS',
  'UPSTREAM_FAILED',
];

export const StatusFilterSelect: FunctionComponent<StatusFilterSelectProps> = props => {
  const { children, ...restProps } = props;

  return (
    <Select {...restProps}>
      {runStatusList.map(status => (
        <Select.Option key={status} value={status}>
          {status}
        </Select.Option>
      ))}
    </Select>
  );
};
