/* eslint-disable no-param-reassign */
import { formatMessage } from 'umi';
import produce from 'immer';
import {
  fetchGlossariesService,
  searchGlossariesService,
  fetchCurrentGlossaryDetailService,
  deleteGlossaryService,
  editGlossaryService,
  EditGlossaryReqBody,
  addGlossaryService,
} from '@/services/glossary';
import { RootDispatch } from '../store';

export interface GlossaryChild {
  id: string;
  name: string;
  description: string;
  childrenCount?: number;
  dataSetCount?: number;
  loading?: boolean;
  parentId?: string;
}

export interface GlossaryNode extends GlossaryChild {
  children?: GlossaryNode[] | null;
  _children?: GlossaryNode[] | null;
  depth?: number;
  verticalIndex?: number;
}

export interface SearchGlossaryItem {
  gid: string;
  resourceType: string;
  name: string;
  description: string;
  resourceAttribute: {
    owners: string;
  };
  ancestryGlossaryList: GlossaryChild[]
  deleted: false;
}

export enum AssetType {
  DATASET = 'dataset',
}

export interface Asset {
  id: string;
  name: string;
  datasource: string;
  database: string;
  description: string;
  owner: string[];
}

export interface GlossaryDetail {
  id: string;
  name: string;
  description: string;
  createUser: string;
  createTime: number | null;
  updateUser: string;
  updateTime: number | null;
  parent: SearchGlossaryItem | null;
  assets: Asset[] | null;
  ancestryGlossaryList: GlossaryChild[];
}

export const getInitGlossaryDetail: () => GlossaryDetail = () => ({
  id: '',
  name: '',
  description: '',
  createUser: '',
  createTime: null,
  updateUser: '',
  updateTime: null,
  parent: null,
  assets: [],
  ancestryGlossaryList: [],
});

export interface GlossaryState {
  searchContent: string;
  glossaryData: GlossaryNode | null;
  fetchRootLoading: boolean;
  autoSuggestGlossaryList: SearchGlossaryItem[];
  currentGlossaryDetail: GlossaryDetail | null;
  fetchCurrentGlossaryDetailLoading: boolean;
}

export interface FetchNodeChildAndUpdateNodeParam {
  nodeData: GlossaryNode;
}

export const glossary = {
  state: {
    searchContent: '',
    glossaryData: null,
    fetchRootLoading: false,
    autoSuggestGlossaryList: [],
    currentGlossaryDetail: null,
    fetchCurrentGlossaryDetailLoading: false,
  } as GlossaryState,
  reducers: {
    updateState: (state: GlossaryState, payload: { key: keyof GlossaryState; value: any }) => ({
      ...state,
      [payload.key]: payload.value,
    }),
    updateFetchRootLoading: produce((draftState: GlossaryState, payload: boolean) => {
      draftState.fetchRootLoading = payload;
    }),
    updateFetchCurrentGlossaryDetailLoading: produce((draftState: GlossaryState, payload: boolean) => {
      draftState.fetchCurrentGlossaryDetailLoading = payload;
    }),
  },
  effects: (dispatch: RootDispatch) => {
    let searchGlossariesFlag = 0;
    return {
      async fetchRootNodeChildGlossary() {
        try {
          dispatch.glossary.updateFetchRootLoading(true);
          const resp = await fetchGlossariesService();
          if (resp) {
            const { children } = resp;
            const rootGlossary: GlossaryNode = {
              id: 'root',
              name: formatMessage({
                id: 'glossary.title',
              }),
              description: '',
              childrenCount: children.length,
              children,
            };
            dispatch.glossary.updateState({
              key: 'glossaryData',
              value: rootGlossary,
            });
            dispatch.glossary.updateFetchRootLoading(false);
          }
        } catch (e) {
          // do nothing
        }
      },

      async fetchNodeChildAndUpdateNode(payload: FetchNodeChildAndUpdateNodeParam) {
        const { nodeData } = payload;
        const { id } = nodeData;
        if (id === 'root') {
          dispatch.glossary.fetchRootNodeChildGlossary();
        } else {
          nodeData.loading = true;
          try {
            const resp = await fetchGlossariesService(id);
            if (resp) {
              const { children } = resp;
              nodeData.children = children.map(child => ({
                ...child,
                parentId: id,
              }));
            }
          } catch (e) {
            // do nothing
          } finally {
            nodeData.loading = false;
          }
        }
        return nodeData;
      },

      async searchGlossaries(payload: string) {
        searchGlossariesFlag += 1;
        const currentSearchGlossariesFlag = searchGlossariesFlag;
        try {
          const resp = await searchGlossariesService(payload, 30);
          if (currentSearchGlossariesFlag === searchGlossariesFlag && resp) {
            dispatch.glossary.updateState({
              key: 'autoSuggestGlossaryList',
              value: resp.searchedInfoList || [],
            });
          }
        } catch (e) {
          // do nothing
        }
      },

      async fetchGlossaryDetail(id: string) {
        dispatch.glossary.updateFetchCurrentGlossaryDetailLoading(true);
        try {
          const resp = await fetchCurrentGlossaryDetailService(id);
          if (resp) {
            dispatch.glossary.updateState({
              key: 'currentGlossaryDetail',
              value: resp,
            });
            return resp;
          }
        } catch (e) {
          // do nothing
        } finally {
          dispatch.glossary.updateFetchCurrentGlossaryDetailLoading(false);
        }
        return null;
      },

      async deleteGlossary(id: string) {
        try {
          const resp = await deleteGlossaryService(id);

          if (resp) {
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async editGlossary(
        {
          id,
          params,
        }: {
          id: string;
          params: EditGlossaryReqBody;
        },
      ) {
        try {
          const resp = await editGlossaryService(id, params);
          if (resp) {
            dispatch.glossary.updateState({
              key: 'currentGlossaryDetail',
              value: resp,
            });
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async addGlossary(params: EditGlossaryReqBody) {
        try {
          const resp = await addGlossaryService(params);
          if (resp) {
            dispatch.glossary.updateState({
              key: 'currentGlossaryDetail',
              value: resp,
            });
            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },
    };
  },
};
