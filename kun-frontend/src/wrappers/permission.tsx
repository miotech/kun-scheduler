import React from 'react';
// import { Redirect } from 'umi';
import useRedux from '@/hooks/useRedux';

import usePermissions from '@/hooks/usePermissions';
// import useDefaultPage from '@/hooks/useDefaultPage';
import Error401 from '@/pages/error-page/Error401';

interface Props {
  children: React.ReactNode;
  route: any;
}

export default function Path({ children, route }: Props) {
  const { permissions } = route;
  const { selector } = useRedux(state => ({
    isLogin: state.user.isLogin,
  }));
  const hasPermission = usePermissions(permissions);
  // const defaultPage = useDefaultPage();

  if (selector.isLogin) {
    if (hasPermission) {
      return children;
    }
    // if (defaultPage) {
    //   return <Redirect to={defaultPage} />;
    // }
    return <Error401 />;
  }

  return children;
}
