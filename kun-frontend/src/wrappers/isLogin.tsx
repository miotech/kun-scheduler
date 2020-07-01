import React, { useEffect } from 'react';
import useRedux from '@/hooks/useRedux';

interface Props {
  children: React.ReactNode;
}

export default function IsLogin({ children }: Props) {
  const { selector, dispatch } = useRedux(state => ({
    isLogin: state.user.isLogin,
  }));

  useEffect(() => {
    if (!selector.isLogin) {
      dispatch.user.fetchWhoami();
    }
  }, [dispatch.user, selector.isLogin]);

  return children;
}
