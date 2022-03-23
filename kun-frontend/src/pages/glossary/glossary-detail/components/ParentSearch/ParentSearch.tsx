import React, { memo, useCallback, useEffect, useState } from 'react';
import { Select } from 'antd';
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
  currentId?: string;
  setCurrentId: (id: string) => void;
}

const { Option } = Select;

export default memo(function ParentSearch({
  isEditting,
  selectedParent,
  onChange,
  disabledId,
  currentId,
  setCurrentId,
}: Props) {
  const t = useI18n();
  const [keyword, setKeyword] = useState(selectedParent ? selectedParent.name : '');
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
        const resp = await searchGlossariesService(debounceKeyword, 30, currentId);
        if (resp && !ignore) {
          setGlossaryList(resp.searchedInfoList.filter(i => i.id !== disabledId));
        }
      }
    };
    search();
    return () => {
      ignore = true;
    };
  }, [debounceKeyword, disabledId, currentId]);

  const handleSearch = useCallback(v => {
    setKeyword(v);
  }, []);

  const handleSelect = useCallback(
    (v, option) => {
      onChange({ id: option.key, name: option.label });
    },
    [onChange],
  );

  const options = glossaryList.map(item => ({ value: item.name, id: item.gid, description: item.description }));

  if (isEditting) {
    return (
      <Select
        className={styles.container}
        filterOption={false}
        onSelect={handleSelect}
        onSearch={handleSearch}
        showSearch
        placeholder={t('common.searchContent')}
      >
        {options.map((item: any) => (
          <Option key={item.id} value={item.id} label={item.label}>
            <span className={styles.name}>{item.value}</span> <span className={styles.des}> {item.description}</span>
          </Option>
        ))}
      </Select>
    );
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
