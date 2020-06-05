import React, { useEffect } from 'react';
import { useRouteMatch } from 'umi';
import { useDispatch } from 'react-redux'
import { RootDispatch } from '@/rematch/store';


interface Props {
  children: React.ReactNode;
}

export default function Path({ children }: Props) {
  const match = useRouteMatch();
  const dispatch = useDispatch<RootDispatch>()

  useEffect(() => {
    dispatch.route.updateCurrentParams(match.params);
    dispatch.route.updateCurrentPath(match.path);
  }, [dispatch, match])

  return children;
}
