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
import { deepFirstSearch, deleteNodeFromParent, addNodeToParent } from '@/utils/glossaryUtiles';
import { RootDispatch, RootState } from '../store';

export interface GlossaryChild {
  id: string;
  name: string;
  description: string;
  childrenCount?: number;
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
  id: string;
  name: string;
}

export enum AssetType {
  DATASET = 'dataset',
}

export interface Asset {
  id: string;
  name: string;
  datasource: string;
  database: string;
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
              depth: undefined,
              verticalIndex: undefined,
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
          const resp = await searchGlossariesService(payload, 10);
          if (currentSearchGlossariesFlag === searchGlossariesFlag && resp) {
            dispatch.glossary.updateState({
              key: 'autoSuggestGlossaryList',
              value: resp.glossaries || [],
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

      async deleteGlossary(id: string, rootState: RootState) {
        try {
          const resp = await deleteGlossaryService(id);

          if (resp) {
            if (resp.parentId) {
              deleteNodeFromParent(id, resp.parentId, rootState.glossary.glossaryData);
            } else if (rootState.glossary.glossaryData) {
              if (rootState.glossary.glossaryData?.children) {
                // eslint-disable-next-line no-param-reassign
                rootState.glossary.glossaryData.children = rootState.glossary.glossaryData.children.filter(
                  child => child.id !== id,
                );
                rootState.glossary.glossaryData.childrenCount = rootState.glossary.glossaryData.children.length;
              }
            }
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
        rootState: RootState,
      ) {
        // 首先从之前的父节点将子节点除去
        const oldParentId = rootState.glossary.currentGlossaryDetail?.parent?.id;

        try {
          const resp = await editGlossaryService(id, params);
          if (resp) {
            dispatch.glossary.updateState({
              key: 'currentGlossaryDetail',
              value: resp,
            });
            const currentNode = deepFirstSearch(rootState.glossary.glossaryData, resp.id);

            if ((oldParentId || params.parentId) && oldParentId !== params.parentId) {
              if (oldParentId) {
                deleteNodeFromParent(id, oldParentId, rootState.glossary.glossaryData);
              } else if (rootState.glossary.glossaryData?.children) {
                rootState.glossary.glossaryData.children = rootState.glossary.glossaryData.children.filter(
                  child => child.id !== id,
                );
                rootState.glossary.glossaryData.childrenCount = rootState.glossary.glossaryData.children.length;
              }

              if (resp.parent && resp.parent.id) {
                addNodeToParent(currentNode, resp.parent.id, rootState.glossary.glossaryData);
                if (currentNode) {
                  currentNode.name = resp.name;
                  currentNode.parentId = resp.parent?.id;
                }
              }
            }

            return resp;
          }
        } catch (e) {
          // do nothing
        }
        return null;
      },

      async addGlossary(params: EditGlossaryReqBody, rootState: RootState) {
        try {
          const resp = await addGlossaryService(params);
          if (resp) {
            dispatch.glossary.updateState({
              key: 'currentGlossaryDetail',
              value: resp,
            });

            const { id, name, description, parent } = resp;
            const newGlossary: GlossaryNode = {
              id,
              name,
              description,
              parentId: parent?.id,
              depth: undefined,
              verticalIndex: undefined,
            };
            if (parent && parent.id) {
              addNodeToParent(newGlossary, parent.id, rootState.glossary.glossaryData);
            } else if (rootState.glossary.glossaryData) {
              if (rootState.glossary.glossaryData?.children) {
                rootState.glossary.glossaryData.children = [newGlossary, ...rootState.glossary.glossaryData.children];
                rootState.glossary.glossaryData.childrenCount = rootState.glossary.glossaryData.children.length;
              } else {
                rootState.glossary.glossaryData.children = [newGlossary];
                rootState.glossary.glossaryData.childrenCount = rootState.glossary.glossaryData.children.length;
              }
            }
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
