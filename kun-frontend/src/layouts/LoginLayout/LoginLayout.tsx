import React, { memo } from 'react';
import c from 'clsx';

import './LoginLayout.global.less';

import asideImg from '@/assets/images/login-sider-bg.jpg';

interface OwnProps {
  className?: string;
  children?: React.ReactNode | string;
}

type Props = React.ComponentProps<'div'> & OwnProps;

export const LoginLayout: React.FC<Props> = memo((props) => {
  const { children, className, ...restProps } = props;

  return (
    <div
      id="login-layout"
      className={c('login-layout', className)}
      {...restProps}
    >
      <aside className="login-layout-aside">
        <img
          className="login-layout-aside__bg-img"
          src={asideImg}
          alt="kun-login-aside-seaside-background"
        />
      </aside>
      <div className="login-layout-content">
        {children}
      </div>
    </div>
  );
});
