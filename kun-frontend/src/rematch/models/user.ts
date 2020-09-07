import { history } from 'umi';
import produce from 'immer';

import { loginService, whoamiService, logoutService } from '@/services/user';

import { RootDispatch } from '../store';

export interface UserState {
  isLogin: boolean;
  username: string;
  whoamiLoading: boolean;

  permissions: string[];
}

export const user = {
  state: {
    isLogin: false,
    username: '',
    whoamiLoading: false,
    permissions: [],
  } as UserState,

  reducers: {
    updateLogin: produce((draftState: UserState, payload: boolean) => {
      draftState.isLogin = payload;
    }),
    updateUserInfo: produce((draftState: UserState, payload) => {
      draftState.username = payload.username;
      draftState.permissions = payload.permissions;
    }),
    updateWhoamiLoading: produce((draftState: UserState, payload: boolean) => {
      draftState.whoamiLoading = payload;
    }),
  },

  effects: (dispatch: RootDispatch) => ({
    async fetchLogin(payload: { username: string; password: string }) {
      try {
        const resp = await loginService(payload);
        if (resp) {
          const whoamiResp = await whoamiService();
          if (whoamiResp) {
            dispatch.user.updateLogin(true);
            dispatch.user.updateUserInfo({
              username: whoamiResp.username,
              permissions: whoamiResp.permissions,
            });
            history.push('/');
          }
        }
        return resp;
      } catch (e) {
        // do nothing
      }
      return null;
    },

    async fetchWhoami() {
      dispatch.user.updateWhoamiLoading(true);
      try {
        const resp = await whoamiService();
        if (resp) {
          dispatch.user.updateLogin(true);
          dispatch.user.updateUserInfo({
            username: resp.username,
            permissions: resp.permissions,
          });
          if (history.location.pathname === '/login') {
            history.push('/');
          }
        } else if (history.location.pathname !== '/login') {
          history.push('/login');
        }
      } catch (e) {
        if (history.location.pathname !== '/login') {
          history.push('/login');
        }
      } finally {
        dispatch.user.updateWhoamiLoading(false);
      }
    },

    async fetchLogout() {
      try {
        const resp = await logoutService();
        if (resp) {
          dispatch.user.updateLogin(false);
        }
      } catch (e) {
        // do nothing
      }
    },
  }),
};
