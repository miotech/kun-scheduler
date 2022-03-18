import React, { memo, useCallback, useEffect } from 'react';
import { useHistory } from 'umi';
import { AutoComplete } from 'antd';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import styles from './AutosuggestInput.less';

const { Option } = AutoComplete;

export default memo(function AutosuggestInput() {
  const history = useHistory();
  const t = useI18n();

  const { selector, dispatch } = useRedux(state => state.glossary);

  const debounceKeyword = useDebounce(selector.searchContent, 300);

  useEffect(() => {
    if (debounceKeyword) {
      dispatch.glossary.searchGlossaries(debounceKeyword);
    }
  }, [debounceKeyword, dispatch.glossary]);

  const handleChange = useCallback(
    v => {
      dispatch.glossary.updateState({ key: 'searchContent', value: v });
    },
    [dispatch.glossary],
  );
  const options = selector.autoSuggestGlossaryList.map(item => ({
    value: item.name,
    id: item.gid,
    description: item.description,
  }));
  
  const handleSelect = useCallback(
    (v, option) => {
      history.push(`/data-discovery/glossary?glossaryId=${option.key}`);
    },
    [history],
  );



  return (
    <AutoComplete
      className={styles.container}
      filterOption={false}
      onSelect={handleSelect}
      onChange={handleChange}
      placeholder={t('glossary.searchGlossary')}
    >
      {options.map((item: any) => (
        <Option key={item.id} value={item.value}>
          <span className={styles.name}>{item.value}</span> <span className={styles.des}> {item.description}</span>
        </Option>
      ))}
    </AutoComplete>
  );
});
