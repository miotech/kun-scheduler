import React, { useState } from 'react';
import Card from '@/components/Card/Card';
import SideCard from './components/SideCard/SideCard';

import styles from './index.less';

export default function Lineage() {
  const [isExpanded, setIsExpanded] = useState(false);
  return (
    <div className={styles.page}>
      <Card className={styles.content}>
        123
        <SideCard
          isExpanded={isExpanded}
          onClickExpandButton={v => setIsExpanded(v)}
        >
          1234
        </SideCard>
      </Card>
    </div>
  );
}
