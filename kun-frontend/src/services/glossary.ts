import SafeUrlAssembler from 'safe-url-assembler';
import {
  GlossaryChild,
  SearchGlossaryItem,
  GlossaryDetail,
  Asset,
} from '@/rematch/models/glossary';
import { get, post, deleteFunc } from './utils';

export interface FetchGlossariesResp {
  parentId: string;
  children: GlossaryChild[];
}

export async function fetchGlossariesService(parentId?: string) {
  const resp = await get<FetchGlossariesResp>('/metadata/glossary/children', {
    parentId,
  });
  return resp;
}

export interface SearchGlossariesServiceResp {
  glossaries: SearchGlossaryItem[];
}
export async function searchGlossariesService(
  keyword: string,
  pageSize: number,
) {
  const resp = await get<SearchGlossariesServiceResp>(
    '/metadata/glossaries/search',
    { pageSize, keyword },
  );
  return resp;
}

export interface FetchCurrentGlossaryDetailResp extends GlossaryDetail {}

export async function fetchCurrentGlossaryDetailService(id: string) {
  const resp = await get<GlossaryDetail>(
    SafeUrlAssembler('/metadata/glossary/:id/detail')
      .param({ id })
      .toString(),
  );
  return resp;
}

export async function deleteGlossaryService(id: string) {
  const resp = await deleteFunc<{ id: string; parentId: string }>(
    SafeUrlAssembler('/metadata/glossary/:id')
      .param({ id })
      .toString(),
  );
  return resp;
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
  const resp = await post<GlossaryDetail>(
    SafeUrlAssembler('/metadata/glossary/:id/update')
      .param({ id })
      .toString(),
    params,
  );
  return resp;
}

export async function addGlossaryService(params: EditGlossaryReqBody) {
  const resp = await post<GlossaryDetail>('/metadata/glossary/add', params);
  return resp;
}

export interface SearchAssetsServicePesp {
  datasets: Asset[];
}

export async function searchAssetsService(keyword: string) {
  const resp = await get<SearchAssetsServicePesp>('/metadata/datasets/search', {
    keyword,
  });
  return resp;
}
