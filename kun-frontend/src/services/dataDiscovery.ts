import { Pagination, Sort } from '@/rematch/models';
import {
  Dataset,
  SearchParamsObj,
  dbFilterItem,
} from '@/rematch/models/dataDiscovery';
import { get } from './utils';

export interface FetchAllTagsServiceRespBody {
  tags: string[];
}

export async function fetchAllTagsService() {
  const resp = await get<FetchAllTagsServiceRespBody>('/metadata/tags/search', {
    keyword: '',
  });
  return resp;
}

export interface FetchAllUsersServiceRespBody {
  users: string[];
}

export async function fetchAllUsersService() {
  const resp = await get<FetchAllUsersServiceRespBody>('/user/search', {
    keyword: '',
  });
  return resp;
}

export interface SearchDatasetsServiceRespBody extends Pagination, Sort {
  datasets: Dataset[];
}

export async function searchDatasetsService(
  search: SearchParamsObj,
  pagination: Pagination,
) {
  const { pageSize, pageNumber } = pagination;
  const params = {
    pageSize,
    pageNumber,
    ...search,
  };
  const resp = await get<SearchDatasetsServiceRespBody>(
    '/metadata/datasets',
    params,
  );
  return resp;

  // // TODO: 假数据
  // return {
  //   datasets: [
  //     {
  //       id: '1',
  //       name: 'table1',
  //       schema: 'public',
  //       description: 'dummy description',
  //       lifecycle: Lifecycle.PROCESS,
  //       database: 'DEV Postgres',
  //       high_watermark: {
  //         user: 'admin',
  //         time: 14000000000,
  //       },
  //       tags: ['a', 'b'],
  //       owners: ['sasdfas', 'sdfsdaf', 'ggggg'],
  //     },
  //   ],
  //   pageSize: 15,
  //   totalCount: 150,
  //   pageNumber: 1,
  //   sortColumn: 'name',
  //   sortOrder: 'ASC',
  // };
}

export interface SearchAllDbServiceResp {
  databases: dbFilterItem[];
}

export async function searchAllDbService(keyword: string) {
  const resp = await get<SearchAllDbServiceResp>('/metadata/databases/search', {
    keyword,
    pageSize: 1000000,
  });
  return resp;
}
