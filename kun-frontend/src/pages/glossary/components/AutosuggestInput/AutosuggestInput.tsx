import React, { memo, useCallback, useEffect } from 'react';
import { Select } from 'antd';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import { GlossaryChild } from '@/rematch/models/glossary';
import styles from './AutosuggestInput.less';

const { Option } = Select;
interface Props {
  onSelect: (id: string) => void; // 选择option事件
  onPathClick?: (id: string) => void; // 点击option路径事件
  onBlur?: () => void;
}
export default memo(function AutosuggestInput({ onPathClick, onSelect, onBlur }: Props) {
  const t = useI18n();

  const { selector, dispatch } = useRedux(state => state.glossary);

  const debounceKeyword = useDebounce(selector.searchContent, 300);

  useEffect(() => {
    if (debounceKeyword) {
      dispatch.glossary.searchGlossaries(debounceKeyword);
    }
  }, [debounceKeyword, dispatch.glossary]);

  const handleSearch = useCallback(
    v => {
      dispatch.glossary.updateState({ key: 'searchContent', value: v });
    },
    [dispatch.glossary],
  );

  const options = selector.autoSuggestGlossaryList.map(item => ({
    value: item.name,
    id: item.gid,
    description: item.description,
    ancestryGlossaryList: item.ancestryGlossaryList,
  }));

  const handleSelect = useCallback(
    (v, option) => {
      onSelect(option.value);
    },
    [onSelect],
  );

  return (
    <Select
      className={styles.container}
      filterOption={false}
      onSelect={handleSelect}
      onSearch={handleSearch}
      onBlur={onBlur}
      showSearch
      placeholder={t('glossary.searchGlossary')}
    >
      {options.map((item: any) => (
        <Option key={item.id} value={item.id} className={styles.option}>
          <div className={styles.label}>
            <span className={styles.name}>{item.value}</span> <span className={styles.des}> {item.description}</span>
          </div>
          {onPathClick &&
            item.ancestryGlossaryList &&
            item.ancestryGlossaryList.map((idx: GlossaryChild, index: number) => {
              return (
                <span
                  key={idx.id}
                  className={styles.pathName}
                  onClick={e => {
                    e.stopPropagation();
                    onPathClick(idx.id);
                  }}
                >
                  {' '}
                  {index !== 0 && '->'} <span className={styles.childName}>{idx.name}</span>
                </span>
              );
            })}
        </Option>
      ))}
    </Select>
  );
});
