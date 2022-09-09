import React, { memo } from 'react';
import c from 'clsx';
import { Tooltip } from 'antd';
import './TextContainer.less';

interface Props {
  children: React.ReactNode;
  maxWidth?: number;
  className?: string;
  ellipsis?: boolean;
  tooltipTitle?: string;
  mouseEnterDelay?: number;
}

export default memo(function TextContainer({
  children,
  maxWidth,
  className,
  ellipsis,
  tooltipTitle,
  mouseEnterDelay,
}: Props) {
  if (ellipsis) {
    return (
      <Tooltip title={tooltipTitle || children} mouseEnterDelay={mouseEnterDelay}>
        <div className={c('TextContainer-ellipsis', className)} style={{ maxWidth }}>
          {children}
        </div>
      </Tooltip>
    );
  }
  return (
    <div className={c('TextContainer', className)} style={{ maxWidth }}>
      {children}
    </div>
  );
});
