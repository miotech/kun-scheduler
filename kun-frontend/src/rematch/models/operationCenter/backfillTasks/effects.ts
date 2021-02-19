import { RootDispatch } from '@/rematch/store';
import {
  fetchBackfillDetail,
  fetchBackfillPage,
  SearchBackfillParams,
} from '@/services/data-backfill/backfill.services';
import { BackfillDetail } from '@/definitions/Backfill.type';

export const effects = (dispatch: RootDispatch) => ({
  async loadBackfillData(payload: SearchBackfillParams) {
    dispatch.backfillTasks.setTableIsLoading(true);
    try {
      const data = await fetchBackfillPage(payload);
      const backfillIds = (data?.records || []).map(rec => rec.id);
      const promises: Promise<BackfillDetail>[] = backfillIds.map(
        id =>
          new Promise((resolve, reject) => {
            fetchBackfillDetail(id)
              .then(detailData => (detailData ? resolve(detailData) : reject()))
              .catch(reject);
          }),
      );
      const details = await Promise.all(promises);
      if (data) {
        dispatch.backfillTasks.setTotal(data.totalCount);
        dispatch.backfillTasks.setTableData(details);
      }
    } finally {
      dispatch.backfillTasks.setTableIsLoading(false);
    }
  },
});
