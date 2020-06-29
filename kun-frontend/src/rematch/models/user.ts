import { history } from 'umi';
import produce from 'immer';

import { loginService, whoamiService } from '@/services/user';

import { RootDispatch } from '../store';

export interface UserState {
  isLogin: boolean;
  name: string;
  whoamiLoading: boolean;
}

export const user = {
  state: {
    isLogin: false,
    name: '',
    whoamiLoading: false,
  } as UserState,

  reducers: {
    updateLogin: produce((draftState: UserState, payload: boolean) => {
      draftState.isLogin = payload;
    }),
    updateUserInfo: produce((draftState: UserState, payload) => {
      draftState.name = payload.name;
    }),
    updateWhoamiLoading: produce((draftState: UserState, payload: boolean) => {
      draftState.whoamiLoading = payload;
    }),
  },

  effects: (dispatch: RootDispatch) => ({
    async fetchLogin(payload: { username: string; password: string }) {
      const resp = await loginService(payload);
      if (resp) {
        const whoamiResp = await whoamiService();
        if (whoamiResp) {
          dispatch.user.updateLogin(true);
          dispatch.user.updateUserInfo({name: whoamiResp.name});
          history.push('/');
        }
      }
    },

    async fetchWhoami() {
      dispatch.user.updateWhoamiLoading(true);
      const resp = await whoamiService();
      dispatch.user.updateWhoamiLoading(false);
      if (resp) {
        dispatch.user.updateLogin(true);
        dispatch.user.updateUserInfo({name: resp.name});
        history.push('/');
      }
    }
  }),
};
