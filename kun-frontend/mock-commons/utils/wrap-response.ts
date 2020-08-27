export function wrapResponseData<T>(responseData: T) {
  return {
    code: 0,
    note: 'Operation Successful',
    result: responseData,
  };
}

export interface PaginationInfo {
  pageSize: number;
  pageNum: number;
  totalCount: number;
}

export function wrapResponseDataWithPagination<T>(responseData: T[], pagination: Partial<PaginationInfo> = {}) {
  return {
    code: 0,
    note: 'Operation Successful',
    result: {
      pageNum: pagination?.pageNum || 1,
      pageSize: pagination?.pageSize || 100,
      totalCount: (typeof pagination?.totalCount === 'number') ? pagination?.totalCount : responseData.length,
      records: responseData,
    },
  };
}

export function wrapResponseError(error: Error, code: string | number = '500') {
  return {
    code,
    note: error.message,
  };
}
