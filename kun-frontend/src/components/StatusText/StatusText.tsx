import React, { FunctionComponent } from 'react';
import c from 'clsx';
import { DeployStatusEnum, RunStatusEnum } from '@/definitions/StatEnums.type';

import './StatusText.less';

interface StatusTextProps {
  status: RunStatusEnum | DeployStatusEnum;
}

export const StatusText: FunctionComponent<StatusTextProps> = (props) => {
  return (
    <span className={c('status-text', {
      [`status-text--${props.status.toLowerCase()}`]: true,
    })}>
      {props.status}
    </span>
  );
};
