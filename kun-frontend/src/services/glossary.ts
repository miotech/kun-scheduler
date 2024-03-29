import { GlossaryChild, SearchGlossaryItem, GlossaryDetail, Asset } from '@/rematch/models/glossary';
import { delet, get, post } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { GlossaryRoleResponse, GlossaryEditorParams } from '@/definitions/Glossary.type';

export interface FetchGlossariesResp {
  parentId: string;
  children: GlossaryChild[];
}

export async function fetchGlossariesService(parentId?: string) {
  return get<FetchGlossariesResp>('/metadata/glossary/children', {
    query: { parentId },
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface SearchGlossariesServiceResp {
  searchedInfoList: SearchGlossaryItem[];
}
export async function searchGlossariesService(keyword: string, pageSize: number, currentId?: string) {
  return get<SearchGlossariesServiceResp>('/metadata/glossaries/search', {
    query: { pageSize, keyword, currentId },
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface FetchCurrentGlossaryDetailResp extends GlossaryDetail {}

export async function fetchCurrentGlossaryDetailService(id: string) {
  return get<GlossaryDetail>('/metadata/glossary/:id/detail', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function deleteGlossaryService(id: string) {
  return delet<{ id: string; parentId: string }>('/metadata/glossary/:id', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface EditGlossaryReqBody {
  name: string;
  description: string;
  parentId?: string;
  assetIds?: string[];
}
export async function editGlossaryService(id: string, params: EditGlossaryReqBody) {
  return post<GlossaryDetail>('/metadata/glossary/:id/update', {
    pathParams: { id },
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface CopyGlossaryReqBody {
  parentId: string;
  sourceId: string;
  copyOperation: string;
}

export async function copyGlossaryService(params: CopyGlossaryReqBody) {
  return post('/metadata/glossary/copy', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function addGlossaryService(params: EditGlossaryReqBody) {
  return post<GlossaryDetail>('/metadata/glossary/add', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export interface SearchAssetsServicePesp {
  datasets: Asset[];
}

export async function searchAssetsService(keyword: string) {
  return get<SearchAssetsServicePesp>('/metadata/datasets/search', {
    query: { keyword, pageSize: 100, pageNum: 1 },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function updateGlossaryOrderService(id: string, prevId?: string) {
  return post<{}>('/metadata/glossary/:id/graph/update', {
    pathParams: {
      id,
    },
    data: {
      prevId,
    },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function queryGlossaryRole(id?: string) {
  return get<GlossaryRoleResponse>('/role/glossary/operation', {
    query: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function queryGlossaryEditor(id: string) {
  return get<[]>('/role/glossary/editor/:id', {
    pathParams: { id },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function addGlossaryEditor(params: GlossaryEditorParams) {
  return post('/role/addEditor', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function removeGlossaryEditor(params: GlossaryEditorParams) {
  return post('/role/removeEditor', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function addGlossaryDateset(params: { id: string; assetIds: string[] }) {
  return post('/metadata/glossary/add/dataset', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function removeGlossaryDateset(params: { id: string; assetIds: string[] }) {
  return post('/metadata/glossary/remove/dataset', {
    data: params,
    prefix: DEFAULT_API_PREFIX,
  });
}
