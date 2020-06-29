import { initState } from './model-state';
import { reducers } from './reducers';
import { effects } from './effects';

export const deployedTaskDetail = {
  state: initState,
  name: 'deployedTaskDetail',
  effects,
  reducers,
};
