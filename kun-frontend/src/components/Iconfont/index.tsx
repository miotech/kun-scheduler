import React, { ComponentProps, RefForwardingComponent, forwardRef } from 'react';
import c from 'classnames';
import './iconfont.less';

export interface IconfontPropTypes extends ComponentProps<any> {
  type: string;
  gapLeft?: number;
  gapRight?: number;
  ariaHidden?: boolean;
}

export const IconfontSvg: React.FC<IconfontPropTypes> = ({
  type,
  gapRight,
  gapLeft,
  ariaHidden,
  ...restProps
}) => (
  <svg
    {...restProps}
    className={c('icon', 'mio-icon', restProps.className)}
    aria-label="icon"
    aria-hidden={ariaHidden ? 'true' : 'false'}
    style={{
      ...(restProps.style || {}),
      marginRight: `${gapRight || 0}px`,
      marginLeft: `${gapLeft || 0}px`,
    }}
  >
    <use xlinkHref={`#icon-${type}`} />
  </svg>
);

const Iconfont: RefForwardingComponent<HTMLSpanElement, IconfontPropTypes> = (
  { type, gapRight, gapLeft, ariaHidden, ...restProps },
  ref,
) => (
  <span
    {...restProps}
    ref={ref}
    className={c('iconfont', `icon-${type}`, restProps.className)}
    aria-label="icon"
    aria-hidden={ariaHidden ? 'true' : 'false'}
    style={{
      ...(restProps.style || {}),
      marginRight: `${gapRight || 0}px`,
      marginLeft: `${gapLeft || 0}px`,
    }}
  />
);

export default forwardRef(Iconfont);
