import React, { memo, useCallback } from 'react';
import { SearchGlossaryItem } from '@/rematch/models/glossary';
import { CopyOutlined } from '@ant-design/icons';
import AutosuggestInput from '@/pages/glossary/components/AutosuggestInput/AutosuggestInput';

import styles from './ParentSearch.less';

interface Props {
  isEditting: boolean;
  selectedParent?: SearchGlossaryItem | null;
  onChange: (value: SearchGlossaryItem) => void;
  setCurrentId: (id: string) => void;
}

export default memo(function ParentSearch({ isEditting, selectedParent, onChange, setCurrentId }: Props) {
  const handleSelect = useCallback(
    (v, option) => {
      onChange({ id: option.key, name: option.label });
    },
    [onChange],
  );

  if (isEditting) {
    return <AutosuggestInput showPath onSelect={handleSelect} defaultValue={selectedParent?.name} />;
  }
  return (
    <div className={styles.noEditName}>
      <CopyOutlined />
      <span className={styles.name} onClick={() => setCurrentId(selectedParent!.id)}>
        {selectedParent!.name}
      </span>
    </div>
  );
});
