import React from 'react';
import { Spin } from 'antd';
import c from 'clsx';
import { SpinProps } from 'antd/es/spin';
import { KunSpinIndicator } from '@/components/KunSpin/KunSpinIndicator';

import './KunSpin.less';

interface KunSpinProps extends SpinProps {
  asBlock?: boolean;
}

export const KunSpin: React.FC<KunSpinProps> = props => {
  const { className, wrapperClassName, asBlock, ...restProps } = props;

  return (
    <Spin
      wrapperClassName={c('kun-spin-wrapper', wrapperClassName, {
        'kun-spin-wrapper--as-block': !!asBlock,
      })}
      className={c('kun-spin', className, {
        'load-container--as-block': !!asBlock,
      })}
      indicator={KunSpinIndicator}
      {...restProps}
    />
  );
};
