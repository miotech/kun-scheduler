import React, { useEffect, useCallback } from 'react';
import { useHistory } from 'umi';
import { Spin } from 'antd';
import { getQueryVariable, getState, getAuthorizeUri } from '@/utils/ssoUtils';
import { getSSOToken } from '@/services/user';

export default function SSOPage() {
  const history = useHistory();

  const fetchToken = useCallback(
    async (code: string) => {
      try {
        await getSSOToken(code, 'okta');
        history.push('/');
      } catch (e) {
        // eslint-disable-next-line
        console.log('e: ', e);
      }
    },
    [history],
  );

  useEffect(() => {
    const state = getQueryVariable('state');
    const localState = sessionStorage.getItem('state');
    // 判断state防止CSRF攻击
    if (localState !== state) {
      // eslint-disable-next-line no-alert
      alert('state参数无效！');
      const newState = getState();
      sessionStorage.setItem('state', newState);
      window.location.replace(getAuthorizeUri(newState));
    } else {
      // url获取code
      const code = getQueryVariable('code');
      fetchToken(code);
    }
  }, [fetchToken]);

  return (
    <div>
      <Spin spinning>
        <div style={{ height: 500 }} />
      </Spin>
    </div>
  );
}
