import { RootDispatch } from '@/rematch/store';
import {
  fetchBackfillDetail,
  fetchBackfillPage,
  SearchBackfillParams,
} from '@/services/data-backfill/backfill.services';
// import { BackfillDetail } from '@/definitions/Backfill.type';

export const effects = (dispatch: RootDispatch) => ({
  async loadBackfillData(payload: SearchBackfillParams) {
    dispatch.backfillTasks.setTableIsLoading(true);
    try {
      const data = await fetchBackfillPage(payload);
      // const backfillIds = (data?.records || []).map(rec => rec.id);
      // const promises: Promise<BackfillDetail>[] = backfillIds.map(
      //   id =>
      //     new Promise((resolve, reject) => {
      //       fetchBackfillDetail(id)
      //         .then(detailData => (detailData ? resolve(detailData) : reject()))
      //         .catch(reject);
      //     }),
      // );
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
