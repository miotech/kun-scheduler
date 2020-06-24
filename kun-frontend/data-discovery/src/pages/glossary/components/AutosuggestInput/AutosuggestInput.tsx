import React, { memo, useCallback, useEffect } from 'react';
import { useHistory } from 'umi';
import { AutoComplete, Input } from 'antd';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import styles from './AutosuggestInput.less';

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

  const handleSelect = useCallback(
    (v, option) => {
      history.push(`/glossary/${option.id}`);
    },
    [history],
  );

  const options = selector.autoSuggestGlossaryList.map(item => ({
    value: item.name,
    id: item.id,
  }));

  return (
    <AutoComplete
      className={styles.container}
      filterOption={false}
      onSelect={handleSelect}
      onChange={handleChange}
      options={options}
    >
      <Input.Search size="large" placeholder={t('glossary.searchGlossary')} />
    </AutoComplete>
  );
});
