import { Pagination, Sort } from '@/definitions/common-types';
import { Dataset, QueryAttributeListBody } from '@/definitions/Dataset.type';

import { SearchParamsObj } from '@/rematch/models/dataDiscovery';
import { get, post } from '@/utils/requestUtils';
import { SECURITY_API_PRIFIX, DEFAULT_API_PREFIX } from '@/constants/api-prefixes';

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

export async function searchDatasetsService(search: Partial<SearchParamsObj>, pagination: Pagination) {
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

export async function queryAttributeList(params: QueryAttributeListBody) {
  return post('/metadata/attribute/list', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}
