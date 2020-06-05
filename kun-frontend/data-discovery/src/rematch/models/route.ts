import produce from 'immer';

export interface RouteState {
  currentMatchPath: string;
  currentParams?: object | undefined;
}

export const route = {
  state: {
    currentMatchPath: '/',
    currentParams: undefined,
  } as RouteState,

  reducers: {
    updateCurrentPath: produce((draftState: RouteState, payload: string) => {
      draftState.currentMatchPath = payload;
    }),
    updateCurrentParams: produce((draftState: RouteState, payload: object) => {
      draftState.currentParams = payload;
    }),
  },
};
