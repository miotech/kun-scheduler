import { defineConfig } from 'umi';
import path from 'path';
const { PROXY_TARGET } = process.env;

export default defineConfig({
  dynamicImport: {},
  nodeModulesTransform: {
    type: 'none',
  },
  proxy: {
    '/kun/api/v1/': {
      target: PROXY_TARGET || 'http://mdp-dev.miotech.com:9999/',
      changeOrigin: true,
    },
  },
  theme: {
    'primary-color': '#1A73E8',
    'link-color': '#1A73E8',
    'success-color': '#2D3446',
    'error-color': '#FF6336',
    'heading-color': '#2d3446',
    'text-color': '#7A7E87',
    'text-color-secondary': '#7A7E87',
    'border-color-base': '#d9d9d9',
    'border-radius-base': '5px',
    'font-size-lg': '14px',
  },
  lessLoader: {
    modifyVars: {
      hack: `true; @import "~@/styles/variables.less"; @import "~@/styles/mixins.less"`,
    },
  },
  locale: {
    default: 'zh-CN',
    antd: true,
    title: true,
    baseNavigator: true,
    baseSeparator: '-',
  },
  favicon: '/favicon.png',
  title: 'common.app.name',
  routes: [
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
        },
      ],
    },
  ],
});
