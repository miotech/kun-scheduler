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
        path: '.',
        exact: true,
        menuDisplay: false,
        component: 'home/index',
        wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
      },
      {
        title: 'common.pageTitle.monitoringDashboard',
        path: '/monitoring-dashboard',
        icon: 'LineChartOutlined',
        menuDisplay: true,
        showChildren: false,
        breadcrumbLink: true,
        routes: [
          {
            title: 'common.pageTitle.monitoringDashboard',
            path: '.',
            component: 'monitoring-dashboard/index',
            exact: true,
            breadcrumbLink: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
          },
        ],
      },
      {
        title: 'common.pageTitle.dataDiscovery',
        path: '/data-discovery',
        icon: 'DataDiscovery',
        menuDisplay: true,
        showChildren: false,
        breadcrumbLink: true,
        routes: [
          {
            title: 'common.pageTitle.dataDiscovery',
            path: '.',
            component: 'data-discovery/index',
            exact: true,
            breadcrumbLink: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
          },
          {
            title: 'common.pageTitle.datasets',
            path: '/data-discovery/dataset',
            menuDisplay: true,
            icon: 'SnippetsOutlined',
            showChildren: false,
            breadcrumbLink: true,
            routes: [
              {
                title: 'common.pageTitle.datasets',
                path: '.',
                component: 'dataset/index',
                breadcrumbLink: true,
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                title: 'common.pageTitle.datasetDetail',
                path: '/data-discovery/dataset/:datasetId',
                breadcrumbLink: true,
                showChildren: false,
                routes: [
                  {
                    title: 'common.pageTitle.datasetDetail',
                    path: '.',
                    component: 'dataset/dataset-detail/index',
                    breadcrumbLink: true,
                    exact: true,
                    wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
                  },
                  {
                    title: 'common.pageTitle.lineage',
                    path: '/data-discovery/dataset/:datasetId/lineage',
                    component: 'lineage/index',
                    breadcrumbLink: true,
                    exact: true,
                    wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
                  },
                ],
              },
              {
                component: 'error-page/Error404/index',
              },
            ],
          },
          {
            title: 'common.pageTitle.glossary',
            path: '/data-discovery/glossary',
            menuDisplay: true,
            icon: 'SnippetsOutlined',
            showChildren: false,
            breadcrumbLink: true,
            routes: [
              {
                title: 'common.pageTitle.glossary',
                path: '.',
                component: 'glossary/index',
                breadcrumbLink: true,
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                component: 'error-page/Error404/index',
              },
            ],
          },
          {
            title: 'common.pageTitle.referenceData',
            path: '/data-discovery/reference-data',
            menuDisplay: true,
            icon: 'SnippetsOutlined',
            showChildren: false,
            breadcrumbLink: true,
            routes: [
              {
                title: 'common.pageTitle.referenceData',
                path: '.',
                component: 'reference-data/index',
                breadcrumbLink: true,
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                title: 'common.pageTitle.tableConfigration',
                path: 'table-configration',
                component: 'reference-data/databaseTable-configration',
                breadcrumbLink: false,
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                title: 'common.pageTitle.version',
                path: 'version',
                component: 'reference-data/version',
                breadcrumbLink: false,
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                component: 'error-page/Error404/index',
              },
            ],
          },
          {
            component: 'error-page/Error404/index',
          },
        ],
      },
      {
        title: 'common.pageTitle.dataDevelopment',
        path: '/data-development',
        icon: 'DataDevelopment',
        menuDisplay: true,
        breadcrumbLink: true,
        routes: [
          {
            title: 'common.pageTitle.dataDevelopment',
            path: '.',
            exact: true,
            breadcrumbLink: true,
            component: '@/pages/data-development/index',
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/withDnDContext', '@/wrappers/permission'],
          },
          {
            title: 'common.pageTitle.taskDefinition',
            path: '/data-development/task-definition/:taskDefId',
            component: 'data-development/task-definition-config',
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/withDnDContext', '@/wrappers/permission'],
          },
          {
            component: 'error-page/Error404/index',
          },
        ],
      },
      {
        title: 'common.pageTitle.operationCenter',
        menuDisplay: true,
        path: '/operation-center',
        icon: 'OperationCenter',
        showChildren: true,
        routes: [
          {
            title: 'common.pageTitle.operationCenter.dryRunTasks',
            path: './dry-run-tasks',
            menuDisplay: false,
            breadcrumbLink: false,
            routes: [
              {
                title: 'common.pageTitle.operationCenter.dryRunTasks',
                menuDisplay: false,
                path: './:id',
                component: '@/pages/operation-center/dry-run-task-detail',
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                component: 'error-page/Error404/index',
              },
            ],
          },
          {
            title: 'common.pageTitle.operationCenter.scheduledTasks',
            path: './scheduled-tasks',
            menuDisplay: true,
            breadcrumbLink: true,
            icon: 'TaskScheduled',
            routes: [
              {
                title: 'common.pageTitle.operationCenter.scheduledTasks',
                path: '.',
                exact: true,
                component: '@/pages/operation-center/scheduled-tasks',
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                title: 'common.pageTitle.operationCenter.scheduledTasks',
                menuDisplay: false,
                path: './:id',
                component: '@/pages/operation-center/deployed-task-detail',
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                component: 'error-page/Error404/index',
              },
            ],
          },
          {
            title: 'common.pageTitle.operationCenter.scheduledTasks',
            path: './task-run-id',
            menuDisplay: false,
            breadcrumbLink: false,
            routes: [
              {
                title: 'common.pageTitle.operationCenter.scheduledTasks',
                path: './:id',
                exact: true,
                component: '@/pages/operation-center/task-run-id-direct',
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
            ],
          },
          {
            title: 'common.pageTitle.operationCenter.backfillTasks',
            path: './backfill-tasks',
            menuDisplay: true,
            breadcrumbLink: true,
            icon: 'TaskInstant',
            routes: [
              {
                title: 'common.pageTitle.operationCenter.backfillTasks',
                path: '.',
                exact: true,
                component: '@/pages/operation-center/backfill-tasks',
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                title: 'common.pageTitle.operationCenter.backfillTasks',
                path: './:id',
                exact: true,
                component: '@/pages/operation-center/backfill-tasks/backfill-tasks-detail',
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
            ],
          },
          {
            title: 'common.pageTitle.operationCenter.runningStatistics',
            path: './running-statistics',
            menuDisplay: true,
            breadcrumbLink: true,
            icon: 'HistoryOutlined',
            routes: [
              {
                title: 'common.pageTitle.operationCenter.runningStatistics',
                path: '.',
                exact: true,
                component: '@/pages/operation-center/running-statistics',
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
            ],
          },
        ],
      },
      {
        title: 'common.pageTitle.settings',
        menuDisplay: true,
        path: '/settings',
        icon: 'SettingOutlined',
        showChildren: true,
        routes: [
          {
            title: 'common.pageTitle.dataSettings',
            path: './data-source',
            menuDisplay: true,
            icon: 'SettingOutlined',
            breadcrumbLink: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
            routes: [
              {
                title: 'common.pageTitle.dataSettings',
                path: '.',
                component: 'data-settings/index',
                exact: true,
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              },
              {
                title: '详情',
                path: './detail',
                exact: true,
                component: 'data-settings/data-source-detail/index',
                wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
              }
            ]
          },
          {
            title: 'common.pageTitle.variableSettings',
            path: './variables',
            menuDisplay: true,
            icon: 'SettingOutlined',
            component: '@/pages/settings/variable-settings/index',
            breadcrumbLink: true,
            exact: true,
            wrappers: ['@/wrappers/path', '@/wrappers/isLogin', '@/wrappers/permission'],
          },
        ],
      },
      {
        title: 'common.pageTitle.login',
        path: '/login',
        component: 'login/index',
        exact: true,
        wrappers: ['@/wrappers/path', '@/wrappers/isLogin'],
      },
      {
        title: 'login.OAuth',
        path: 'oauth2/login',
        component: 'login/OAuth',
        exact: true,
        wrappers: ['@/wrappers/path'],
      },
      {
        component: 'error-page/Error404/index',
      },
    ],
  },
];
