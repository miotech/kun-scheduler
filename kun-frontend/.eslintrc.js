module.exports = {
  extends: [require.resolve('@umijs/fabric/dist/eslint')],
  globals: {
    API_PREFIX: true,
    PRODUCTION_MODE: true,
    page: true,
    PROXY_TARGET: true,
    DISABLE_MOCK: true,
    MOCK_FUNC_CONFIG: true,
  },
  plugins: ['react-hooks'],
  rules: {
    'semi': [
      'error', 'always'
    ],
    'func-names': [
      'error', 'as-needed'
    ],
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
  parserOptions: {
    tsconfigRootDir: __dirname,
    project: './tsconfig.json',
    /**
     * parserOptions.createDefaultProgram
     * Default .false
     * This option allows you to request that when the setting is specified,
     * files will be allowed when not included in the projects defined by the provided files.
     * Using this option will incur significant performance costs.
     * This option is primarily included for backwards-compatibility.
     * See the project section above for more information.projecttsconfig.json
     */
    createDefaultProgram: true,
  }
};
