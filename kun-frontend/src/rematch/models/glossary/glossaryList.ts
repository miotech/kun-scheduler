import { fetchGlossariesService } from '@/services/glossary';
import produce from 'immer';
import { RootDispatch, RootState } from '../../store';
import { GlossaryChild } from '../glossary';

const updateTreeData = (list: GlossaryListNode[], id: React.Key, children: GlossaryListNode[]): GlossaryListNode[] =>
  list.map(node => {
    if (node.id === id) {
      if (node.children) {
        const newChildren: GlossaryListNode[] = [];
        children.forEach(item => {
          const findItem = node?.children?.find(child => child.id === item.id);
          if (findItem) {
            newChildren.push({ ...item, children: findItem.children });
          } else {
            newChildren.push(item);
          }
        });
        return {
          ...node,
          children: newChildren,
        };
      }
      return {
        ...node,
        isLeaf: children.length === 0,
        children,
      };
    }
    if (node.children) {
      return {
        ...node,
        children: updateTreeData(node.children, id, children),
      };
    }
    return node;
  });

export interface GlossaryListNode extends GlossaryChild {
  children?: GlossaryListNode[];
  isLeaf?: boolean;
}

export interface GlossaryListState {
  glossaryListData: GlossaryListNode[] | null;
  expandedKeys: string[];
}

export const glossaryList = {
  state: {
    glossaryListData: null,
    expandedKeys: [],
  } as GlossaryListState,
  reducers: {
    updateState: produce((state: GlossaryListState, payload: { key: keyof GlossaryListState; value: any }) => ({
      ...state,
      [payload.key]: payload.value,
    })),
  },
  effects: (dispatch: RootDispatch) => ({
    async fetchGlossaryRoot(payload, state: RootState) {
      const res = await fetchGlossariesService();
      if (res) {
        const childrenList = res.children.map(child => ({ ...child, isLeaf: child.childrenCount === 0 }));
        const { glossaryListData }: GlossaryListState = state.glossaryList;
        const newChildren: GlossaryListNode[] = [];
        childrenList.forEach(item => {
          const findItem = glossaryListData?.find((child: GlossaryListNode) => child.id === item.id);
          if (findItem) {
            newChildren.push({ ...item, children: findItem.children });
          } else {
            newChildren.push(item);
          }
        });
        dispatch.glossaryList.updateState({
          key: 'glossaryListData',
          value: newChildren,
        });
      }
    },
    async fetchGlossaryChildren({ glossaryId }: { glossaryId: string }, state: any) {
      const res = await fetchGlossariesService(glossaryId);
      if (res) {
        const childrenList = res.children.map(child => ({ ...child, isLeaf: child.childrenCount === 0 }));
        const newData = updateTreeData(state.glossaryList.glossaryListData, glossaryId, childrenList);
        dispatch.glossaryList.updateState({
          key: 'glossaryListData',
          value: newData,
        });
      }
      return res;
    },
  }),
};
