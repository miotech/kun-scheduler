import { RootDispatch } from '@/rematch/store';
import {
  fetchBackfillDetail,
  fetchBackfillPage,
  SearchBackfillParams,
} from '@/services/data-backfill/backfill.services';

export const effects = (dispatch: RootDispatch) => ({
  async loadBackfillData(payload: SearchBackfillParams) {
    dispatch.backfillTasks.setTableIsLoading(true);
    try {
      const data = await fetchBackfillPage(payload);
      if (data) {
        dispatch.backfillTasks.setTotal(data.totalCount);
        dispatch.backfillTasks.setTableData(data.records);
      }
    } finally {
      dispatch.backfillTasks.setTableIsLoading(false);
    }
  },
  async loadBackfillDetail(backfillId: string) {
    dispatch.backfillTasks.setBackfillDetailIsLoading(true);
    try {
      const payload = await fetchBackfillDetail(backfillId);
      if (payload) {
        dispatch.backfillTasks.setBackfillDetailData(payload);
      }
    } catch (e) {
      dispatch.backfillTasks.setBackfillDetailData(null);
      dispatch.backfillTasks.setBackfillDetailPageError(e);
    } finally {
      dispatch.backfillTasks.setBackfillDetailIsLoading(false);
      dispatch.backfillTasks.setBackfillDetailTableIsReloading(false);
    }
  },

  async refreshBackfillSubTasksTable(backfillId: string) {
    dispatch.backfillTasks.setBackfillDetailTableIsReloading(true);
    try {
      const payload = await fetchBackfillDetail(backfillId);
      if (payload) {
        dispatch.backfillTasks.setBackfillDetailData(payload);
      }
    } catch (e) {
      dispatch.backfillTasks.setBackfillDetailData(null);
      dispatch.backfillTasks.setBackfillDetailPageError(e);
    } finally {
      dispatch.backfillTasks.setBackfillDetailIsLoading(false);
      dispatch.backfillTasks.setBackfillDetailTableIsReloading(false);
    }
  },
});
