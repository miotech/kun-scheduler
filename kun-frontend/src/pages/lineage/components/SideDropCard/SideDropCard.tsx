import React, { memo, useMemo } from 'react';
import c from 'clsx';
import SideCard from '../SideCard/SideCard';
import DatasetCard from '../DatasetCard/DatasetCard';
import RelatedTaskCard from '../RelatedTaskCard/RelatedTaskCard';

interface Props {
  isExpanded: boolean;
  onExpand: (v: boolean) => void;
  datasetId?: string | null;
  sourceDatasetId?: string;
  destDatasetId?: string;
  sourceDatasetName?: string;
  destDatasetName?: string;
  className?: string;
  type: 'dataset' | 'task';
}

export default memo(function SideDropCard({
  datasetId,
  isExpanded,
  onExpand,
  className = '',
  type,
  sourceDatasetId,
  destDatasetId,
  sourceDatasetName,
  destDatasetName,
}: Props) {
  const childrenComp = useMemo(() => {
    if (type === 'dataset' && datasetId) {
      return <DatasetCard datasetId={datasetId} isExpanded={isExpanded} />;
    }
    if (type === 'task' && sourceDatasetId && destDatasetId) {
      return (
        <RelatedTaskCard
          destDatasetId={destDatasetId}
          destDatasetName={destDatasetName || ''}
          sourceDatasetId={sourceDatasetId}
          sourceDatasetName={sourceDatasetName || ''}
        />
      );
    }
    return null;
  }, [
    datasetId,
    destDatasetId,
    destDatasetName,
    isExpanded,
    sourceDatasetId,
    sourceDatasetName,
    type,
  ]);

  return (
    <SideCard
      isExpanded={isExpanded}
      className={c(className)}
      onClickExpandButton={onExpand}
    >
      {childrenComp}
    </SideCard>
  );
});
