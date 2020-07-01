import React, { useCallback } from 'react';
import { Input, Button, message, Form } from 'antd';
import { Store } from 'antd/lib/form/interface';
import { useDispatch } from 'react-redux';
import { RootDispatch } from '@/rematch/store';

import useI18n from '@/hooks/useI18n';

import css from './index.less';

export interface LoginParams {
  username: string;
  password: string;
}

const layout = {
  labelCol: { span: 8 },
  wrapperCol: { span: 16 },
};
const tailLayout = {
  wrapperCol: { offset: 8, span: 16 },
};

export default function Login() {
  const t = useI18n();
  const dispatch = useDispatch<RootDispatch>();

  const handleClickLogin = useCallback(
    (params: LoginParams) => {
      const diss = message.loading(t('common.loading'), 0);
      dispatch.user.fetchLogin(params).then(() => {
        diss();
      });
    },
    [dispatch, t],
  );

  const onFinish = (values: LoginParams) => {
    handleClickLogin(values);
  };

  return (
    <div className={css.login}>
      <div className={css.inputArea}>
        <h2 style={{ textAlign: 'center', marginBottom: 16 }}>
          {t('login.welcome')}
        </h2>
        <Form
          {...layout}
          name="basic"
          onFinish={onFinish as (values: Store) => void}
        >
          <Form.Item
            label={t('login.userName')}
            name="username"
            rules={[
              { required: true, message: t('login.pleaseInput.username') },
            ]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            label={t('login.password')}
            name="password"
            rules={[
              { required: true, message: t('login.pleaseInput.password') },
            ]}
          >
            <Input.Password />
          </Form.Item>

          <Form.Item {...tailLayout}>
            <Button type="primary" htmlType="submit">
              {t('login.confirmButton')}
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}
