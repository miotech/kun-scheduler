import { initState } from './model-state';
import { effects } from './effects';
import { reducers } from './reducers';

export const backfillTasks = {
  state: initState,
  name: 'backfillTasks',
  effects,
  reducers,
};
