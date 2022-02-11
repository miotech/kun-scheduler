import React, { memo } from 'react';
import { CopyOutlined } from '@ant-design/icons';
import LineList from '@/components/LineList/LineList';
import { GlossaryNode } from '@/rematch/models/glossary';
import styles from './ChildrenGlossaryList.less';

interface Props {
  childList: GlossaryNode[];
  setCurrentId: (id: string) => void
}

export default memo(function ChildrenGlossaryList({ childList, setCurrentId }: Props) {

  return (
    <LineList>
      {childList.map(child => (
        <div className={styles.childItem} key={child.id}>
          {/* <FileTextOutlined /> */}
          <CopyOutlined />
          <div className={styles.right}>
            <span className={styles.name} onClick={() => setCurrentId(child.id)}>{child.name} ({child.dataSetCount})</span>
            <div className={styles.description}>{child.description}</div>
          </div>
        </div>
      ))}
    </LineList>
  );
});
