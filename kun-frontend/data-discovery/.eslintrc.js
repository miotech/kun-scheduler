module.exports = {
  extends: [require.resolve('@umijs/fabric/dist/eslint')],
  globals: {
    API_PREFIX: true,
    PRODUCTION_MODE: true,
    pinyinlite: true,
    page: true,
    PROXY_TARGET: true,
    DISABLE_MOCK: true,
    MOCK_FUNC_CONFIG: true,
  },
  plugins: ['react-hooks'],
  rules: {
    'no-param-reassign': [
      'error',
      {
        props: true,
        ignorePropertyModificationsFor: ['draft', 'draftState'],
      },
    ],
    'import/no-extraneous-dependencies': 'off',
    'react-hooks/rules-of-hooks': 'error', // 检查 Hook 的规则
    'react-hooks/exhaustive-deps': 'warn', // 检查 effect 的依赖
  },
};
