import React, { useEffect } from 'react';
import useUrlState from '@ahooksjs/use-url-state';
import { OAuthLogin } from '@/services/user';
import { useDispatch } from 'react-redux';
import { RootDispatch } from '@/rematch/store';
import { history } from 'umi';

export default function OAuth() {
  const [state] = useUrlState({});
  const dispatch = useDispatch<RootDispatch>();

  useEffect(() => {
    OAuthLogin(state).then(res => {
      dispatch.user.updateLogin(true);
      dispatch.user.updateUserInfo({
        username: res.username,
        permissions: res.permissions || [],
      });
      history.push('/');
    });
  }, [state, dispatch.user]);
  return <div />;
}
