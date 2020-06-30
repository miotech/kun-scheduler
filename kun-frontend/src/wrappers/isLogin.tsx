import React from 'react';
// import { useDispatch, useSelector } from 'react-redux';
// import { RootDispatch, RootState } from '@/rematch/store';

interface Props {
  children: React.ReactNode;
}

export default function IsLogin({ children }: Props) {
  // const isLogin = useSelector((state: RootState) => state.user.isLogin);
  // const dispatch = useDispatch<RootDispatch>();

  // useEffect(() => {
  //   if (!isLogin) {
  //     dispatch.user.fetchWhoami();
  //   }
  // }, [isLogin, dispatch]);

  return children;
}
