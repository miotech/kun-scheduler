import React from 'react';
import { Spin } from 'antd';
import c from 'classnames';
import { SpinProps } from 'antd/es/spin';

import './KunSpin.less';

interface KunSpinProps extends SpinProps {
  asBlock?: boolean;
}

const defaultIndicator = (
  <div className="load-container">
    <div className="container">
      <div className="boxLoading boxLoading1" />
      <div className="boxLoading boxLoading2" />
      <div className="boxLoading boxLoading3" />
      <div className="boxLoading boxLoading4" />
      <div className="boxLoading boxLoading5" />
    </div>
  </div>
);

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
      indicator={defaultIndicator}
      {...restProps}
    />
  );
};
