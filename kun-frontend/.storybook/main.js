const path = require('path');
const webpack = require('webpack');

/**
 * The main configuration file of Storybook: https://github.com/storybookjs/storybook
 * See ref: https://storybook.js.org/docs/react/configure/overview for further configuration options.
 *
 * The main.js configuration file is a preset and as such has a powerful interface, but the key fields within it are:
 *
 * stories - an array of globs that indicates the location of your story files, relative to main.js.
 * addons - a list of the addons you are using.
 * webpackFinal - custom webpack configuration.
 * babel - custom babel configuration.
 */
module.exports = {
  stories: [
    '../src/**/*.stories.@(jsx|tsx|mdx)',
  ],
  addons: [
    /**
     * Storybook essentials includes the following addons. Addons can be disabled and re-configured as described below:
     *
     * Actions
     * Backgrounds
     * Controls
     * Docs
     * Viewport
     * Toolbars
     */
    '@storybook/addon-essentials',
    '@storybook/addon-a11y',
    '@storybook/addon-knobs',
    '@storybook/addon-jest',
    '@storybook/addon-storysource',
  ],
  typescript: {
    check: false,
    checkOptions: {},
    reactDocgen: 'react-docgen-typescript',
    reactDocgenTypescriptOptions: {
      shouldExtractLiteralValuesFromEnum: true,
      propFilter: (prop) => (prop.parent ? !/node_modules/.test(prop.parent.fileName) : true),
    },
  },
  webpackFinal: async (config, { configType }) => {
    // `configType` has a value of 'DEVELOPMENT' or 'PRODUCTION'
    // You can change the configuration based on that.
    // 'PRODUCTION' is used when building the static version of storybook.

    // Make whatever fine-grained changes you need
    config.module.rules.push({
      test: /\.less$/i,
      use: [
        {
          loader: 'style-loader',
        },
        {
          loader: 'css-loader',
          options: {
            importLoaders: 1,
            modules: {
              auto: true,
              localIdentName: '[name]__[local]--[hash:base64:5]',
            },
            sourceMap: true,
          },
        },
        {
          loader: 'less-loader',
          options: {
            lessOptions: {
              paths: [
                path.resolve(__dirname, '..'),
                path.resolve(__dirname, '../node_modules'),
              ],
              modifyVars: {
                hack: `true; @import "~@/styles/variables.less"; @import "~@/styles/mixins.less"`,
              },
            },
          },
        },
      ],
      include: path.resolve(__dirname, '../'),
    });
    config.module.rules.unshift({
      test: /\.svg(\?v=\d+\.\d+\.\d+)?$/i,
      use: [
        {
          loader: 'babel-loader',
          options: {
            babelrc: false,
            presets: [
              // '@babel/preset-env',
              '@babel/preset-react',
            ]
          },
        },
        {
          loader: '@svgr/webpack',
          options: {
            babel: false,
            icon: true,
            svgo: true,
          },
        },
      ],
    })

    config.resolve = {
      ...config.resolve,
      alias: {
        ...config.resolve.alias,
        '@': path.resolve(__dirname, '../src'),
        '@@': path.resolve(__dirname, '../src/.umi'),
      },
    };

    config.plugins = [
      ...config.plugins,
      new webpack.DefinePlugin({
        LOG_LEVEL: 2,
      }),
    ];

    // Return the altered config
    return config;
  },
};
