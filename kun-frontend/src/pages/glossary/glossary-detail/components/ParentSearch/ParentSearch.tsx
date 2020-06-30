import React, { memo, useCallback, useEffect, useState } from 'react';
import { AutoComplete, Input } from 'antd';
import { Link } from 'umi';
import useI18n from '@/hooks/useI18n';
import useDebounce from '@/hooks/useDebounce';
import { SearchGlossaryItem } from '@/rematch/models/glossary';
import { searchGlossariesService } from '@/services/glossary';
import { CopyOutlined } from '@ant-design/icons';

import styles from './ParentSearch.less';

interface Props {
  isEditting: boolean;
  selectedParent?: SearchGlossaryItem | null;
  onChange: (value: SearchGlossaryItem) => void;
  disabledId?: string;
}

export default memo(function ParentSearch({
  isEditting,
  selectedParent,
  onChange,
  disabledId,
}: Props) {
  const t = useI18n();
  const [keyword, setKeyword] = useState(
    selectedParent ? selectedParent.name : '',
  );
  useEffect(() => {
    if (selectedParent) {
      setKeyword(selectedParent.name);
    }
  }, [selectedParent]);

  const [glossaryList, setGlossaryList] = useState<SearchGlossaryItem[]>(() => {
    if (selectedParent) {
      return [selectedParent];
    }
    return [];
  });

  const debounceKeyword = useDebounce(keyword, 300);

  useEffect(() => {
    let ignore = false;
    const search = async () => {
      if (debounceKeyword) {
        const resp = await searchGlossariesService(debounceKeyword, 10);
        if (resp && !ignore) {
          setGlossaryList(resp.glossaries.filter(i => i.id !== disabledId));
        }
      }
    };
    search();
    return () => {
      ignore = true;
    };
  }, [debounceKeyword, disabledId]);

  const handleChange = useCallback(v => {
    setKeyword(v);
  }, []);

  const handleSelect = useCallback(
    (v, option) => {
      handleChange(v);
      onChange(option);
    },
    [handleChange, onChange],
  );

  const options = glossaryList.map(item => ({ value: item.name, id: item.id }));

  if (isEditting) {
    return (
      <AutoComplete
        className={styles.container}
        filterOption={false}
        onSelect={handleSelect}
        onChange={handleChange}
        options={options}
        value={keyword}
      >
        <Input.Search
          value={keyword}
          size="large"
          placeholder={t('common.searchContent')}
        />
      </AutoComplete>
    );
  }
  return (
    <div className={styles.noEditName}>
      <CopyOutlined />
      <Link to={`/data-discovery/glossary/${selectedParent!.id}`}>
        <span className={styles.name}>{selectedParent!.name}</span>
      </Link>
    </div>
  );
});
