import { init, RematchDispatch, RematchRootState } from '@rematch/core';
import createLoadingPlugin from '@rematch/loading';

import { LoadingState } from '@/definitions/common-types';
// eslint-disable-next-line import/named
import { models, RootModel } from './models';

/**
 * rematch loading plugin.
 * Reference: https://rematch.github.io/rematch/#/plugins/loading
 */
const loadingPlugin = createLoadingPlugin({
  name: 'loading',
  blacklist: [],
});

export const store = init({
  models,
  plugins: [
    loadingPlugin,
  ],
});

export type Store = typeof store;
export type RootDispatch = RematchDispatch<RootModel>;
// @ts-ignore
export type RootState = RematchRootState<RootModel> & LoadingState<RootModel>;
