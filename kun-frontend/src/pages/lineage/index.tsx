import { useRouteMatch } from 'umi';
import React, { useState } from 'react';
import Button from 'antd/lib/button';
import Card from '@/components/Card/Card';
import SideDropCard from './components/SideDropCard/SideDropCard';

import styles from './index.less';

export default function Lineage() {
  const match = useRouteMatch<{ datasetId: string }>();
  const [isExpanded, setIsExpanded] = useState(false);
  const [currentType, setCurrentType] = useState<'dataset' | 'task'>('dataset');
  return (
    <div className={styles.page}>
      <Card className={styles.content}>
        123
        <Button
          onClick={() => {
            setCurrentType('dataset');
            setIsExpanded(true);
          }}
        >
          dataset
        </Button>
        <Button
          onClick={() => {
            setCurrentType('task');
            setIsExpanded(true);
          }}
        >
          task
        </Button>
        <SideDropCard
          isExpanded={isExpanded}
          datasetId={match.params.datasetId}
          sourceDatasetId="63968095944835072"
          destDatasetId="63969133489815552"
          sourceDatasetName="source dataset"
          destDatasetName="dest dataset"
          onExpand={(v: boolean) => setIsExpanded(v)}
          type={currentType}
        />
      </Card>
    </div>
  );
}
