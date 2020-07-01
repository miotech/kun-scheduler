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
        path: '/',
        exact: true,
        redirect: '/data-discovery',
      },
      {
        title: 'common.pageTitle.dataDiscovery',
        path: '/data-discovery',
        icon: 'FileTextOutlined',
        menuDisplay: true,
        showChildren: false,
        routes: [
          {
            title: 'common.pageTitle.dataDiscovery',
            path: '.',
            component: 'index/index',
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
          },
          {
            title: 'common.pageTitle.datasets',
            path: '/data-discovery/dataset',
            menuDisplay: true,
            icon: 'SnippetsOutlined',
            showChildren: false,
            routes: [
              {
                title: 'common.pageTitle.datasets',
                path: '.',
                component: 'dataset/index',
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
              },
              {
                title: 'common.pageTitle.datasetDetail',
                path: '/data-discovery/dataset/:datasetId',
                component: 'dataset/dataset-detail/index',
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
              },
            ],
          },

          {
            title: 'common.pageTitle.glossary',
            path: '/data-discovery/glossary',
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
                path: '/data-discovery/glossary/create',
                component: 'glossary/glossary-detail/index',
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
              },
              {
                title: 'common.pageTitle.glossaryDetail',
                path: '/data-discovery/glossary/:glossaryId',
                component: 'glossary/glossary-detail/index',
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
              },
            ],
          },
        ],
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
        title: 'common.pageTitle.login',
        path: '/login',
        component: 'login/index',
        exact: true,
        wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
      },
    ],
  },
];
