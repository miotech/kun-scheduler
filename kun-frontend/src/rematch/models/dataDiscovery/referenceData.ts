import { ReferenceDataState } from '@/definitions/ReferenceData.type';
// import res from '@/pages/reference-data/databaseTable-configration/test.json';

export const referenceData = {
  state: {
    refTableData: null,
  } as ReferenceDataState,
  reducers: {
    updateState: (
      state: ReferenceDataState,
      payload: { key: keyof ReferenceDataState; value: any },
    ) => ({
      ...state,
      [payload.key]: payload.value,
    }),
  },
  effects: {

  }
};