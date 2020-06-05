import React, { memo } from 'react';
import { useSelector, shallowEqual } from 'react-redux';
import { Layout } from 'antd';

import { RootState } from '@/rematch/store';
import logo from '@/assets/images/kun-logo.png';

import useI18n from '@/hooks/useI18n';

import css from './Header.less';

const { Header: AntdHeader } = Layout;

export default memo(function Header() {
  const t = useI18n();

  const userInfo = useSelector(
    (state: RootState) => ({
      useName: state.user.name,
      isLogin: state.user.isLogin,
    }),
    shallowEqual,
  );

  return (
    <AntdHeader className={css.header}>
      <span className={css.logoArea}>
        <img className={css.logoImage} src={logo} alt="Logo" />
        <span className={css.appName}>{t('common.app.name')}</span>
      </span>
      {userInfo.isLogin ? (
        <span className={css.userInfo}>{userInfo.useName}</span>
      ) : (
        <span className={css.userInfo}>登录</span>
      )}
    </AntdHeader>
  );
});
