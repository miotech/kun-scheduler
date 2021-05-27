import React, { useMemo } from 'react';
import { Breadcrumb as AntdBreadcrumb } from 'antd';
import { IRoute, Link } from 'umi';
import { shallowEqual, useSelector } from 'react-redux';
import { RootState } from '@/rematch/store';
import * as pathToRegexp from 'path-to-regexp';

import './Breadcrumb.less';
// import useI18n from '@/hooks/useI18n';

interface Props {
  route: IRoute;
}

function Breadcrumb({ route }: Props) {
  const selectedData = useSelector(
    (state: RootState) => ({
      currentPath: state.route.currentMatchPath,
      currentParams: state.route.currentParams,
      datasetName: state.datasetDetail.name,
      glossaryName: state.glossary.currentGlossaryDetail?.name,
      deployedTaskName: state.deployedTaskDetail.deployedTask?.name,
      backfillName: state.backfillTasks.backfillDetail?.data?.name,
    }),
    shallowEqual,
  );

  // const t = useI18n();

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

  const realPth = (path: string, params: object) => pathToRegexp.compile(path)(params);

  const getLinkComp = (routeItem: IRoute) => {
    switch (routeItem.path) {
      case '/':
        return null;
      case '/data-discovery/glossary/:glossaryId':
        return (
          <Link to={realPth(routeItem.path || '/', selectedData.currentParams || {})}>
            {selectedData.glossaryName ?? routeItem.title}
          </Link>
        );
      case '/operation-center/scheduled-tasks/:id':
        return (
          <Link to={realPth(routeItem.path || '/', selectedData.currentParams || {})}>
            {selectedData.deployedTaskName ?? '...'}
          </Link>
        );
      case '/operation-center/backfill-tasks/:id':
        return (
          <Link to={realPth(routeItem.path || '/', selectedData.currentParams || {})}>
            {selectedData.backfillName ?? '...'}
          </Link>
        );
      default:
        break;
    }
    if (!routeItem.breadcrumbLink) {
      return <span>{routeItem.title}</span>;
    }
    return <Link to={realPth(routeItem.path || '/', selectedData.currentParams || {})}>{routeItem.title}</Link>;
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
