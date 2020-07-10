import React, { memo } from 'react';
import { Link } from 'umi';
import { CopyOutlined } from '@ant-design/icons';
import LineList from '@/components/LineList/LineList';
import { GlossaryNode } from '@/rematch/models/glossary';
import useBackPath from '@/hooks/useBackPath';
import styles from './ChildrenGlossaryList.less';

interface Props {
  childList: GlossaryNode[];
}

export default memo(function ChildrenGlossaryList({ childList }: Props) {
  const { getBackPath } = useBackPath();

  return (
    <LineList>
      {childList.map(child => (
        <div className={styles.childItem} key={child.id}>
          {/* <FileTextOutlined /> */}
          <CopyOutlined />
          <Link to={getBackPath(`/data-discovery/glossary/${child.id}`)}>
            <span className={styles.name}>{child.name}</span>
          </Link>
        </div>
      ))}
    </LineList>
  );
});
