import React, { memo, useMemo, useCallback } from 'react';
import { Layout, Menu, Dropdown } from 'antd';
import { DownOutlined } from '@ant-design/icons';

import { RootState } from '@/rematch/store';
import useRedux from '@/hooks/useRedux';
import logo from '@/assets/images/kun-logo.png';

import useI18n from '@/hooks/useI18n';

import I18nSwitch from '../I18nSwitch/I18nSwitch';
import css from './Header.less';

const { Header: AntdHeader } = Layout;

export default memo(function Header() {
  const t = useI18n();

  const { selector, dispatch } = useRedux((state: RootState) => ({
    username: state.user.username,
    isLogin: state.user.isLogin,
  }));

  const handleClickLogout = useCallback(() => {
    dispatch.user.fetchLogout();
  }, [dispatch.user]);

  const dropdownMenu = useMemo(
    () => (
      <Menu>
        <Menu.Item onClick={handleClickLogout}>
          <span>{t('header.logout')}</span>
        </Menu.Item>
      </Menu>
    ),
    [handleClickLogout, t],
  );

  return (
    <AntdHeader className={css.header}>
      <span className={css.logoArea}>
        <img className={css.logoImage} src={logo} alt="Logo" />
        {/* <span className={css.appName}>{t('common.app.name')}</span> */}
      </span>

      <I18nSwitch className={css.i18nSwitch} />

      {selector.isLogin ? (
        <Dropdown trigger={['click']} overlay={dropdownMenu}>
          <span className={css.userInfo}>
            {selector.username} <DownOutlined />
          </span>
        </Dropdown>
      ) : (
        <span className={css.userInfo}>登录</span>
      )}
    </AntdHeader>
  );
});
