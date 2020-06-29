import { initState } from './model-state';
import { reducers } from './reducers';
import { effects } from './effects';

export const dataDevelopment = {
  state: initState,
  name: 'dataDevelopment',
  effects,
  reducers,
};
