import React, { useCallback, useMemo } from 'react';
import { Input, Button, message, Form, notification } from 'antd';
import { useDispatch } from 'react-redux';
import useI18n from '@/hooks/useI18n';

import { LoginLayout } from '@/layouts/LoginLayout';
import { LoginFooter } from '@/pages/login/LoginFooter';

import { RootDispatch } from '@/rematch/store';
import { Store } from 'antd/lib/form/interface';

import kunLogo from '@/assets/images/kun-logo.png';

import css from './index.less';

export interface LoginParams {
  username: string;
  password: string;
}

const layout = {
  wrapperCol: { span: 24 },
};

const displayFooter = true;

export default function Login() {
  const t = useI18n();
  const dispatch = useDispatch<RootDispatch>();

  const handleClickLogin = useCallback(
    (params: LoginParams) => {
      const diss = message.loading(t('common.loading'), 0);
      dispatch.user.fetchLogin(params).then(resp => {
        if (resp && resp.code !== 0) {
          notification.error({
            message: t('login.error.usernamePasswordError'),
          });
        }
        diss();
      });
    },
    [dispatch, t],
  );

  const onFinish = useCallback(
    (values: LoginParams) => {
      handleClickLogin(values);
    },
    [handleClickLogin],
  );

  const formContent = useMemo(
    () => (
      <div className={css.inputArea}>
        <div className={css.Logo}>
          <img className="app-logo" src={kunLogo} alt="kun-logo" />
          <h4 id="app-name" className="app-name">
            {t('common.app.name')}
          </h4>
        </div>
        <Form
          {...layout}
          name="basic"
          onFinish={onFinish as (values: Store) => void}
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: t('login.pleaseInput.username') },
            ]}
          >
            <Input size="large" placeholder={t('login.userName')} />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: t('login.pleaseInput.password') },
            ]}
          >
            <Input.Password size="large" placeholder={t('login.password')} />
          </Form.Item>

          <Form.Item>
            <Button size="large" block type="primary" htmlType="submit">
              {t('login.confirmButton')}
            </Button>
          </Form.Item>
        </Form>
      </div>
    ),
    [onFinish, t],
  );

  return (
    <LoginLayout>
      {formContent}
      {displayFooter ? <LoginFooter /> : null}
    </LoginLayout>
  );
}
