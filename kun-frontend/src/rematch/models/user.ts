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
      const resp = await loginService(payload);
      if (resp && resp.code === 0) {
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
    },

    async fetchWhoami() {
      dispatch.user.updateWhoamiLoading(true);
      const resp = await whoamiService();
      dispatch.user.updateWhoamiLoading(false);
      if (resp) {
        dispatch.user.updateLogin(true);
        dispatch.user.updateUserInfo({
          username: resp.username,
          permissions: resp.permissions,
        });
      }
    },

    async fetchLogout() {
      const resp = await logoutService();
      if (resp) {
        dispatch.user.updateLogin(false);
      }
    },
  }),
};
