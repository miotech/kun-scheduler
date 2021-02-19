import React, { memo, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import { IRoute, Link } from 'umi';
import { Layout, Menu } from 'antd';
import take from 'lodash/take';
import { RootState } from '@/rematch/store';
import hasOptionalPermissions from '@/utils/hasOptionalPermissions';

import { iconComponentMap } from './iconmap';

import css from './Sider.less';

const { Sider: AntdSider } = Layout;
const { SubMenu } = Menu;

interface Props {
  route: IRoute;
}

interface IShowRouter extends IRoute {
  menuDisplay?: boolean;
  icon?: any;
  showChildren?: boolean;
}

export default memo(function Sider({ route }: Props) {
  const currentPath = useSelector(
    (state: RootState) => state.route.currentMatchPath,
  );

  const permissions = useSelector((state: RootState) => state.user.permissions);

  const { routes } = route;

  const selectedKeys = useMemo(() => {
    const pathList = currentPath.split('/');
    const usablePathList = pathList;
    const colonIndex = pathList.findIndex(path => path.includes(':'));
    if (colonIndex !== -1) {
      usablePathList.splice(colonIndex, usablePathList.length - colonIndex);
    }
    const resultList: string[] = [];
    usablePathList.forEach((i, index) => {
      resultList.push(take(usablePathList, index + 1).join('/') || '/');
    });
    return resultList;
  }, [currentPath]);

  const menu = useMemo(() => {
    const getChildComp = (routeItem: IShowRouter) => {
      if (routeItem) {
        if (!hasOptionalPermissions(permissions, routeItem.permissions)) {
          return null;
        }
      }
      if (routeItem.menuDisplay) {
        const { icon } = routeItem;
        if (routeItem.routes && routeItem.showChildren) {
          const childCompArray = routeItem.routes
            .map(i => getChildComp(i as IShowRouter))
            .filter(i => !!i);
          return (
            <SubMenu
              key={routeItem.path}
              icon={iconComponentMap[icon]}
              title={routeItem.title}
            >
              {childCompArray}
            </SubMenu>
          );
        }
        return (
          <Menu.Item
            key={routeItem.path}
            icon={iconComponentMap[icon]}
            title={routeItem.title}
          >
            <Link to={routeItem.path || '/'}>{routeItem.title}</Link>
          </Menu.Item>
        );
      }
      return null;
    };

    const resultMenu = (
      <Menu style={{ marginTop: 20 }} mode="inline" selectedKeys={selectedKeys}>
        {routes?.map(i => getChildComp(i as IShowRouter)).filter(i => !!i)}
      </Menu>
    );

    return resultMenu;
  }, [permissions, routes, selectedKeys]);

  const [collapsed, setCollapsed] = useState(true);

  return (
    <AntdSider
      className={css.sider}
      collapsible
      collapsed={collapsed}
      onCollapse={setCollapsed}
      collapsedWidth={48}
      theme="light"
    >
      {menu}
    </AntdSider>
  );
});
