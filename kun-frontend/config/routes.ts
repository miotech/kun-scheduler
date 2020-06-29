/**
 * Umi application route definition
 * @author Josh Ouyang 06/29/2020
 */

export const appRoutes = [
  {
    path: '/',
    component: '@/layouts/index',
    title: 'common.pageTitle.homepage',
    routes: [
      {
        title: 'common.pageTitle.homepage',
        path: '/',
        component: 'index/index',
        exact: true,
        wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
      },
      {
        title: 'common.pageTitle.dataDiscovery',
        path: '/data-discovery',
        menuDisplay: true,
        icon: 'FileTextOutlined',
        showChildren: false,
        routes: [
          {
            title: 'common.pageTitle.dataDiscovery',
            path: '.',
            component: 'data-discovery/index',
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
          },
          {
            title: 'common.pageTitle.datasetDetail',
            path: ':datasetId',
            component: 'data-discovery/dataset-detail/index',
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
          },
        ],
      },
      {
        title: 'common.pageTitle.dataDevelopment',
        path: '/data-dev',
        menuDisplay: true,
        icon: 'ToolOutlined',
        component: 'data-development/index',
        wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
      },
      {
        title: 'common.pageTitle.dataSettings',
        path: '/data-settings',
        menuDisplay: true,
        icon: 'SettingOutlined',
        component: 'data-settings/index',
        wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
      },
      {
        title: 'common.pageTitle.glossary',
        path: '/glossary',
        menuDisplay: true,
        icon: 'SnippetsOutlined',
        showChildren: false,
        routes: [
          {
            title: 'common.pageTitle.glossary',
            path: '.',
            component: 'glossary/index',
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
          },
          {
            title: 'common.pageTitle.glossaryCreate',
            path: '/glossary/create',
            component: 'glossary/glossary-detail/index',
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
          },
          {
            title: 'common.pageTitle.glossaryDetail',
            path: ':glossaryId',
            component: 'glossary/glossary-detail/index',
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
          },
        ],
      },
      {
        title: 'common.pageTitle.login',
        path: '/login',
        component: 'login/index',
        exact: true,
      },
    ],
  },
];
