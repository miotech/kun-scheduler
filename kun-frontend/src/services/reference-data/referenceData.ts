import { get, post } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { DataBase, FetchRdmDatasParams } from '@/definitions/ReferenceData.type';
import { QueryAttributeListParams } from '@/definitions/ResourceAttribute.type';

export async function parseData(data: any) {
  return post('rdm/data/parse', {
    data,
    prefix: DEFAULT_API_PREFIX,
  });
}

// 发布指定版本
export async function publish(versionId: string) {
  return post('/rdm/publish/:versionId', {
    pathParams: { versionId },
    prefix: DEFAULT_API_PREFIX,
  });
}

// 获取指定table的ref data
export async function getTableConfigurationByTableId(tableId: string) {
  return get('/rdm/data/edit/:tableId', {
    pathParams: { tableId },
    prefix: DEFAULT_API_PREFIX,
  });
}

// 获取指定版本的ref data
export async function getTableConfigurationByVersionId(versionId: string) {
  return get('/rdm/data/:versionId', {
    pathParams: { versionId },
    prefix: DEFAULT_API_PREFIX,
  });
}

// 编辑ref table info
export async function editTableConfiguration(data: any) {
  return post('rdm/edit', {
    data,
    prefix: DEFAULT_API_PREFIX,
  });
}

// 获取版本历史
export async function getVersionList(tableId: string) {
  return get('/rdm/info/:tableId', {
    pathParams: { tableId },
    prefix: DEFAULT_API_PREFIX,
  });
}

// 停用指定版本
export async function deactivateVersionAPI(versionId: string) {
  return post('/rdm/deactivate/:versionId', {
    pathParams: { versionId },
    prefix: DEFAULT_API_PREFIX,
  });
}

// 回滚指定版本
export async function rollbackVersionAPI(versionId: string) {
  return post('/rdm/rollback/:versionId', {
    pathParams: { versionId },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function getTableList(params: { pageNum: number; pageSize: number }) {
  return get('/rdm/info/table/page', {
    query: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

// 获取数据库列表
export async function getDatabases() {
  return get<DataBase[]>('/rdm/databases', {
    prefix: DEFAULT_API_PREFIX,
  });
}

// validate rdm
export async function validRdm(data: any) {
  return post('/rdm/valid', {
    data,
    prefix: DEFAULT_API_PREFIX,
  });
}

/**
 * @param params
 * @returns
 */
export const fetchRdmDatas = (params: FetchRdmDatasParams) => {
  return post('/rdm/info/table/page/search', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
};

/**
 * @param params
 * @returns
 */
export const queryRdmAttributeList = (params: QueryAttributeListParams) => {
  return post('/rdm/attribute/list', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
};
