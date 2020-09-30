import { initState } from './model-state';
import { reducers } from './reducers';
import { effects } from './effects';

export const monitoringDashboard = {
  state: initState,
  name: 'monitoringDashboard',
  effects,
  reducers,
};
