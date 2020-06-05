import React, { useState, useCallback } from 'react';
import { Input, Button, message } from 'antd';
import { useDispatch } from 'react-redux';
import { RootDispatch } from '@/rematch/store';

import useI18n from '@/hooks/useI18n';

import css from './index.less';

export default function Login() {
  const t = useI18n();
  const dispatch = useDispatch<RootDispatch>();

  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');

  const handleClickLogin = useCallback(() => {
    const diss = message.loading(t('common.loading'), 0);
    dispatch.user.fetchLogin({ username: userName, password }).then(() => {
      diss();
    });
  }, [dispatch, userName, password, t]);

  return (
    <div className={css.login}>
      <div className={css.inputArea}>
        <h2 style={{ textAlign: 'center', marginBottom: 16 }}>
          {t('login.welcome')}
        </h2>
        <Input
          style={{ marginBottom: 16 }}
          addonBefore={t('login.userName')}
          value={userName}
          onChange={e => setUserName(e.target.value)}
        />
        <Input
          style={{ marginBottom: 16 }}
          type="password"
          addonBefore={t('login.password')}
          value={password}
          onChange={e => setPassword(e.target.value)}
        />

        <Button
          disabled={!userName || !password}
          type="primary"
          onClick={handleClickLogin}
        >
          {t('login.confirmButton')}
        </Button>
      </div>
    </div>
  );
}
