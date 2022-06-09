import React, { memo } from 'react';
import { Tree, Popover } from 'antd';
import { GlossaryListNode } from '@/rematch/models/glossary';
import useRedux from '@/hooks/useRedux';
import styles from './TreeList.less';

const { DirectoryTree } = Tree;

interface Props {
  glossaryId: string;
  setCurrentGlossaryId: (glossaryId: string) => void;
}

const PopoverContent = (des: string) => {
  return <div className={styles.popover}>{des}</div>;
};
const TitleRender: any = (nodeData: GlossaryListNode) => {
  return (
    <div className={styles.titleRender}>
      <div className={styles.title}>{nodeData.name}</div>
      <Popover content={PopoverContent(nodeData.description)} placement="bottom">
        <div className={styles.des}>{nodeData.description}</div>
      </Popover>
    </div>
  );
};

const TreeView = memo((props: Props) => {
  const { glossaryId, setCurrentGlossaryId } = props;
  const { selector, dispatch } = useRedux<any>(state => state.glossaryList);
  const { glossaryListData, expandedKeys } = selector;
  const onExpand = (newExpandedKeys: React.Key[]) => {
    dispatch.glossaryList.updateState({
      key: 'expandedKeys',
      value: newExpandedKeys,
    });
  };
  const onLoadData = (node: any) =>
    new Promise<void>(resolve => {
      const { id } = node;
      dispatch.glossaryList.fetchGlossaryChildren({ glossaryId: id }).then(() => {
        resolve();
      });
    });

  const onSelect = (selectedKeys: React.Key[]) => {
    if (selectedKeys.length) {
      setCurrentGlossaryId(selectedKeys[0] as string);
    }
  };

  return (
    <DirectoryTree
      onSelect={onSelect}
      onExpand={onExpand}
      expandedKeys={expandedKeys}
      selectedKeys={[glossaryId]}
      loadData={onLoadData}
      treeData={glossaryListData}
      titleRender={TitleRender}
      fieldNames={{ title: 'name', key: 'id' }}
    />
  );
});

export default TreeView;
