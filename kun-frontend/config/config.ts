import { defineConfig } from 'umi';
import path from 'path';
import { appRoutes } from './routes';
import { theme } from './theme';

const { PROXY_TARGET } = process.env;

export default defineConfig({
  dynamicImport: {},
  hash: true,
  nodeModulesTransform: {
    type: 'none',
  },
  proxy: {
    '/kun/api/v1/': {
      target: PROXY_TARGET || 'http://mdp-dev.miotech.com:9999/',
      changeOrigin: true,
    },
  },
  theme,
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
  favicon: '/favicon.ico',
  title: 'common.app.name',
  routes: appRoutes,
});
