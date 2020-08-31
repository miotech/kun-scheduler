import {
  GlossaryChild,
  SearchGlossaryItem,
  GlossaryDetail,
  Asset,
} from '@/rematch/models/glossary';
import { delet, get, post } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';

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
  glossaries: SearchGlossaryItem[];
}
export async function searchGlossariesService(
  keyword: string,
  pageSize: number,
) {
  return get<SearchGlossariesServiceResp>('/metadata/glossaries/search', {
    query: { pageSize, keyword },
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
export async function editGlossaryService(
  id: string,
  params: EditGlossaryReqBody,
) {
  return post<GlossaryDetail>('/metadata/glossary/:id/update', {
    pathParams: { id },
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
    query: { keyword },
    prefix: DEFAULT_API_PREFIX,
  });
}
