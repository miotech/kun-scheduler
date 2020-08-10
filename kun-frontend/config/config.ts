import { IConfig, defineConfig } from 'umi';
import path from 'path';
import { appRoutes } from './routes';
import { theme } from './theme';
import CopyWebpackPlugin from 'copy-webpack-plugin';

const { PROXY_TARGET, PROXY_PDF_TARGET } = process.env;

export default defineConfig({
  dynamicImport: {},
  hash: true,
  nodeModulesTransform: {
    type: 'none',
  },
  chainWebpack(memo) {
    memo.plugin('copy-cmaps').use(CopyWebpackPlugin, [
      {
        patterns: [
          {
            from: path.join(__dirname, '../', 'node_modules/pdfjs-dist/cmaps/'),
            to: 'cmaps/',
          },
        ],
      },
    ]);
    // memo.module
    //   .rule('parse-pdf')
    //   .test(/\.(pdf)$/)
    //   .use('file-loader')
    //   .options({
    //     name: '[name].[ext]',
    //   });
  },
  targets: {
    ie: 11,
  },
  proxy: {
    '/kun/api/v1/pdf/': {
      target: PROXY_PDF_TARGET || 'http://kun-dev.miotech.com/',
    },
    '/kun/api/v1/': {
      target: PROXY_TARGET || 'http://kun-dev.miotech.com/',
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
}) as IConfig;
