import { IConfig, defineConfig } from 'umi';
import fs from 'fs';
import path from 'path';
import { appRoutes } from './routes';
import { theme } from './theme';
import { define } from './define';

let certConfigTemplate = {
  key: undefined,
  cert: undefined,
};
if (fs.existsSync(path.resolve(__dirname, './certConfig.json'))) {
  certConfigTemplate = {
    ...JSON.parse(
      fs.readFileSync(path.resolve(__dirname, './certConfig.json'), {
        encoding: 'utf-8',
      }),
    ),
  };
}

const {
  PROXY_TARGET,
  PROXY_SECURITY_TARGET,
  PROXY_TARGET_DASHBOARD,
  PROXY_TARGET_DATA_PLATFORM,
  USE_MOCK,
  PATH_REWRITE,
  NO_PROXY,
  PORT,
  HTTPS,
  HTTPS_KEY,
  HTTPS_CERT,
} = process.env;

export default defineConfig({
  dynamicImport: {
    loading: '@/layouts/LoadingLayout/index',
  },
  hash: true,
  nodeModulesTransform: {
    type: 'none',
  },
  targets: {
    ie: 11,
  },
  proxy: !NO_PROXY
    ? {
        '/kun/api/v1/security/': {
          target: PROXY_SECURITY_TARGET || 'http://kun-dev.miotech.com/',
          changeOrigin: true,
          withCredentials: true,
        },
        '/kun/api/v1/dashboard/': {
          target: PROXY_TARGET_DASHBOARD || 'http://kun-dev.miotech.com/',
          changeOrigin: true,
          withCredentials: true,
          pathRewrite: PATH_REWRITE
            ? { '^/kun/api/dashboard/': '' }
            : undefined,
        },
        '/kun/api/v1/': {
          target: PROXY_TARGET || 'http://kun-dev.miotech.com/',
          changeOrigin: true,
          withCredentials: true,
          pathRewrite: PATH_REWRITE ? { '^/kun/api/v1/': '/kun/api/v1/' } : undefined,
        },
        '/kun/api/data-platform/': {
          target:
            PROXY_TARGET_DATA_PLATFORM ||
            PROXY_TARGET ||
            'http://kun-dev.miotech.com/',
          changeOrigin: true,
          withCredentials: true,
          pathRewrite: PATH_REWRITE
            ? { '^/kun/api/data-platform/': '' }
            : undefined,
        },
      }
    : {},
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
  devServer: {
    host: HTTPS ? 'dev.localhost.com' : undefined,
    port: PORT ? parseInt(PORT, 10) : 8000,
    https: HTTPS
      ? {
          key: HTTPS_KEY || certConfigTemplate.key,
          cert: HTTPS_CERT || certConfigTemplate.cert,
        }
      : undefined,
  },
  define,
  mock: USE_MOCK === 'true' ? {} : false,
  // modify webpack rules
  /*
  async chainWebpack(memo) {
    memo.module.rule('svg')
      .test(/\.svg(\?v=\d+\.\d+\.\d+)?$/)
      // .include
      //  .add(path.resolve(__dirname, '../src/assets/icons'))
      //  .end()
      .use('svg')
        .loader('babel-loader')
        .options({
          babelrc: false,
          presets: [
            // '@babel/preset-env',
            '@babel/preset-react',
          ],
        })
        .loader('@svgr/webpack')
        .options({
          babel: false,
          icon: true,
          svgo: true,
        })
        .loader('url-loader');
  }
  */
}) as IConfig;
