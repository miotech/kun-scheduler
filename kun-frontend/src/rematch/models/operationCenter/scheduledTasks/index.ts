import { initState } from './model-state';
import { reducers } from './reducers';
import { effects } from './effects';

export const scheduledTasks = {
  state: initState,
  name: 'scheduledTasks',
  effects,
  reducers,
};
