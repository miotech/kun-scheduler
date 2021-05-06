import { Backfill, BackfillDetail } from '@/definitions/Backfill.type';
import { initState, BackfillModelState as ModelState } from './model-state';

export const reducers = {
  resetAll: (): ModelState => ({
    ...initState,
  }),
  setTablePageNum: (state: ModelState, payload: number): ModelState => {
    let nextPageNum = payload;
    if (payload <= 0 || Number.isNaN(payload)) {
      nextPageNum = 1;
    }
    return {
      ...state,
      filters: {
        ...state.filters,
        pageNum: nextPageNum,
      },
    } as ModelState;
  },
  setTablePageSize: (state: ModelState, payload: number): ModelState => {
    let nextPageSize = payload;
    if (payload <= 0 || Number.isNaN(payload)) {
      nextPageSize = 1;
    }
    return {
      ...state,
      filters: {
        ...state.filters,
        pageSize: nextPageSize,
      },
    } as ModelState;
  },
  setTotal: (state: ModelState, payload: number): ModelState => {
    return { ...state, total: payload };
  },
  setTableIsLoading: (state: ModelState, payload: boolean): ModelState => {
    return {
      ...state,
      tableIsLoading: payload,
    } as ModelState;
  },
  setTableData: (state: ModelState, payload: Backfill[]): ModelState => {
    return {
      ...state,
      tableData: payload,
    } as ModelState;
  },
  updateFilter: (state: ModelState, payload: Partial<ModelState['filters']>): ModelState => {
    return {
      ...state,
      filters: {
        ...state.filters,
        ...payload,
      },
    };
  },
  setBackfillDetailIsLoading: (state: ModelState, payload: boolean): ModelState => {
    return {
      ...state,
      backfillDetail: {
        ...state.backfillDetail,
        isLoading: payload,
      },
    };
  },
  setBackfillDetailTableIsReloading: (state: ModelState, payload: boolean): ModelState => {
    return {
      ...state,
      backfillDetail: {
        ...state.backfillDetail,
        tableIsReloading: payload,
      },
    };
  },
  setBackfillDetailData: (state: ModelState, payload: BackfillDetail | null): ModelState => {
    return {
      ...state,
      backfillDetail: {
        ...state.backfillDetail,
        data: payload,
      },
    };
  },
  setBackfillDetailPageError: (state: ModelState, payload: Error | null): ModelState => {
    return {
      ...state,
      backfillDetail: {
        ...state.backfillDetail,
        pageError: payload || null,
      },
    };
  },
};
