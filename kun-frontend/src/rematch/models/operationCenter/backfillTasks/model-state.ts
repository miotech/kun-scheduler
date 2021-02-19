import { BackfillDetail } from '@/definitions/Backfill.type';
import { Moment } from 'moment';

export interface BackfillModelState {
  filters: {
    pageNum: number;
    pageSize: number;
    keyword: string;
    creatorId: string | null;
    startTimeRng: Moment | null;
    endTimeRng: Moment | null;
  };
  tableData: BackfillDetail[];
  tableIsLoading: boolean;
  total: number;
}

export const initState: BackfillModelState = {
  filters: {
    pageNum: 1,
    pageSize: 25,
    keyword: '',
    creatorId: null,
    startTimeRng: null,
    endTimeRng: null,
  },
  tableData: [],
  tableIsLoading: false,
  total: 0,
};
