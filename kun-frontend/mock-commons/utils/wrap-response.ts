export function wrapResponseData<T>(responseData: T) {
  return {
    code: 0,
    note: 'Operation Successful',
    result: responseData,
  };
}

export interface PaginationInfo {
  pageSize: number;
  pageNumber: number;
  totalCount: number;
}

export function wrapResponseDataWithPagination<T>(responseData: T[], pagination: Partial<PaginationInfo> = {}, recordsFieldName: string = 'records') {
  return {
    code: 0,
    note: 'Operation Successful',
    result: {
      pageNumber: pagination?.pageNumber || 1,
      pageSize: pagination?.pageSize || 100,
      totalCount: (typeof pagination?.totalCount === 'number') ? pagination?.totalCount : responseData.length,
      [`${recordsFieldName}`]: responseData,
    },
  };
}

export function wrapResponseError(error: Error, code: string | number = '500') {
  return {
    code,
    note: error.message,
  };
}
