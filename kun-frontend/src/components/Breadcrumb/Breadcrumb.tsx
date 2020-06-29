import React, { useMemo } from 'react';
import { Breadcrumb as AntdBreadcrumb } from 'antd';
import { IRoute, Link } from 'umi';
import { shallowEqual, useSelector } from 'react-redux';
import { RootState } from '@/rematch/store';
import * as pathToRegexp from 'path-to-regexp';

import './Breadcrumb.less';

interface Props {
  route: IRoute;
}

function Breadcrumb({ route }: Props) {
  const selectedData = useSelector(
    (state: RootState) => ({
      currentPath: state.route.currentMatchPath,
      currentParams: state.route.currentParams,
      datasetName: state.datasetDetail.name,
    }),
    shallowEqual,
  );

  const titlePathArray = useMemo(() => {
    const list: IRoute[] = [];
    let useList: IRoute[] | undefined;

    const findPath = (childRoute: IRoute, thePath: string) => {
      if (useList) return;
      list.push(childRoute);
      if (childRoute.path === thePath) {
        useList = list;
        return;
      }
      if (childRoute.routes) {
        childRoute.routes.forEach(i => {
          findPath(i, thePath);
        });
      }
      if (useList) return;
      list.pop();
    };

    findPath(route, selectedData.currentPath);

    return useList || [];
  }, [selectedData.currentPath, route]);

  const realPth = (path: string, params: object) =>
    pathToRegexp.compile(path)(params);

  const getLinkComp = (routeItem: IRoute) => {
    if (routeItem.path === '/data-discovery/:datasetId') {
      return (
        <Link
          to={realPth(routeItem.path || '/', selectedData.currentParams || {})}
        >
          {selectedData.datasetName ?? routeItem.title}
        </Link>
      );
    }
    return (
      <Link
        to={realPth(routeItem.path || '/', selectedData.currentParams || {})}
      >
        {routeItem.title}
      </Link>
    );
  };

  return (
    <AntdBreadcrumb>
      {titlePathArray.map(i => (
        <AntdBreadcrumb.Item key={i.path}>{getLinkComp(i)}</AntdBreadcrumb.Item>
      ))}
    </AntdBreadcrumb>
  );
}

export default Breadcrumb;
