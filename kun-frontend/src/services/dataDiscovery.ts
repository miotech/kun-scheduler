import { Pagination, Sort } from '@/definitions/common-types';
import { Dataset } from '@/definitions/Dataset.type';

import {
  SearchParamsObj,
  DsFilterItem,
  DatabaseFilterItem,
} from '@/rematch/models/dataDiscovery';
import { get } from '@/utils/requestUtils';
import {
  DEFAULT_API_PREFIX,
  SECURITY_API_PRIFIX,
} from '@/constants/api-prefixes';

export interface FetchAllTagsServiceRespBody {
  tags: string[];
}

export async function fetchAllTagsService() {
  return get<FetchAllTagsServiceRespBody>('/metadata/tags/search', {
    query: {
      keyword: '',
    },
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface FetchAllUsersServiceRespBody {
  users: string[];
}

export async function fetchAllUsersService() {
  return get<FetchAllUsersServiceRespBody>('/user/search', {
    query: {
      keyword: '',
    },
    prefix: SECURITY_API_PRIFIX,
  });
}

export interface SearchDatasetsServiceRespBody extends Pagination, Sort {
  datasets: Dataset[];
}

export async function searchDatasetsService(
  search: Partial<SearchParamsObj>,
  pagination: Pagination,
) {
  const { pageSize, pageNumber } = pagination;
  const params = {
    pageSize,
    pageNumber,
    ...search,
  };
  return get<SearchDatasetsServiceRespBody>('/metadata/datasets', {
    query: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface SearchAllDsServiceResp {
  datasources: DsFilterItem[];
}

export async function searchAllDsService(keyword: string) {
  return get<SearchAllDsServiceResp>('/metadata/datasources/search', {
    query: {
      keyword,
      pageSize: 1000000,
    },
    prefix: DEFAULT_API_PREFIX,
  });
}

export type FetchAllDbService = DatabaseFilterItem[];
export async function fetchAllDbService(params?: { dataSourceIds: string[] }) {
  return get<SearchAllDsServiceResp>('/metadata/databases', {
    query: params,
  });
}
