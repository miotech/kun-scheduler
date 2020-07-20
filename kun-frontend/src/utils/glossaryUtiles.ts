import moment from 'moment';
import { GlossaryNode } from '@/rematch/models/glossary';

export const watermarkFormatter = (time?: string | number) => {
  if (time && !Number.isNaN(Number(time))) {
    return moment(Number(time)).format('YYYY-MM-DD HH:mm:ss');
  }
  return '';
};

export const deepFirstSearch = (
  node: GlossaryNode | null,
  targetId: string,
) => {
  if (node != null) {
    const stack: GlossaryNode[] = [];
    stack.push(node);
    while (stack.length !== 0) {
      const item = stack.pop();
      if (item?.id === targetId) {
        return item;
      }
      const children = item?.children ?? [];
      for (let i = children.length - 1; i >= 0; i -= 1) {
        stack.push(children[i]);
      }
    }
  }
  return null;
};

export const deleteNodeFromParent = (
  id: string,
  parentId: string,
  dataGrope: GlossaryNode | null,
) => {
  const parentNodeData = deepFirstSearch(dataGrope, parentId);
  if (parentNodeData) {
    if (parentNodeData.childrenCount) {
      parentNodeData.childrenCount -= 1;
    }
    if (parentNodeData.children) {
      parentNodeData.children = parentNodeData.children.filter(
        child => child.id !== id,
      );
      if (parentNodeData.children.length === 0) {
        parentNodeData.children = null;
      }
    }
  }
};

export const addNodeToParent = (
  currentNode: GlossaryNode | null,
  parentId: string,
  dataGrope: GlossaryNode | null,
) => {
  const newParentNode = deepFirstSearch(dataGrope, parentId);
  if (newParentNode) {
    if (newParentNode.childrenCount) {
      newParentNode.childrenCount += 1;
    } else {
      newParentNode.childrenCount = 1;
    }

    if (newParentNode.children && currentNode) {
      newParentNode.children = [currentNode, ...newParentNode.children];
    }
  }
};
