import React, { memo, useCallback, useEffect, useState } from 'react';
import { Select, Popover } from 'antd';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import useDebounce from '@/hooks/useDebounce';
import { useUpdateEffect } from 'ahooks';
import styles from './AutosuggestInput.less';

const { Option } = Select;
interface Props {
  onSelect?: (id: string, option?: any) => void; // 选择option事件
  onPathClick?: (id: string) => void; // 点击option路径事件
  showPath?: boolean; // 是否展示路径
  onBlur?: () => void;
  defaultValue?: string;
  value?: string;
  mode?: string;
  initOptions?: any[];
  onChange?: (value?: string | string[], option?: any) => void;
}
export default memo(function AutosuggestInput({ onPathClick, onSelect, onBlur, value, defaultValue, showPath, mode, onChange, initOptions }: Props) {
  const t = useI18n();
  const [options, setOptions] = useState([]);
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

  useEffect(() => {
    let newOptions = [];
    if (initOptions?.length) {
      newOptions = initOptions.map(item => ({
        value: item.name,
        id: item.id,
        description: item.description,
        ancestryGlossaryList: item.ancestryGlossaryList,
      }));
    }
    setOptions(newOptions);
  }, [initOptions]);

  useUpdateEffect(() => {
    let newOptions = [];
    if (selector.autoSuggestGlossaryList.length) {
      newOptions = selector.autoSuggestGlossaryList.map(item => ({
        value: item.name,
        id: item.gid,
        description: item.description,
        ancestryGlossaryList: item.ancestryGlossaryList,
      }));
    }
    setOptions(newOptions);
  }, [selector.autoSuggestGlossaryList]);

  const handleSelect = useCallback(
    (v, option) => {
      if (onSelect) {
        onSelect(option.value, option);
      }
    },
    [onSelect],
  );

  const PopoverContent = (value: string, description: string) => {
    return (
      <div className={styles.pophover}>
        <div className={styles.name}>{value}</div> <div className={styles.des}> {description}</div>
      </div>
    );
  };
  return (
    <Select
      className={styles.container}
      filterOption={false}
      onSelect={handleSelect}
      onChange={onChange}
      onSearch={handleSearch}
      onBlur={onBlur}
      defaultValue={defaultValue}
      showSearch
      placeholder={t('glossary.searchGlossary')}
      mode={mode}
      value={value}
    >
      {options.map((item: any) => (
        <Option key={item.id} value={item.id} className={styles.option}>
          <Popover
            mouseEnterDelay={0.5}
            zIndex={10000}
            content={PopoverContent(item.value, item.description)}
            placement="bottom"
          >
            <div className={styles.label}>
              <div className={styles.name}>{item.value}</div> <div className={styles.des}> {item.description}</div>
            </div>
          </Popover>
          {showPath &&
            item.ancestryGlossaryList &&
            item.ancestryGlossaryList.map((idx: GlossaryChild, index: number) => {
              // 不展示路径的根节点
              return (
                <span
                  key={idx.id}
                  className={styles.pathName}
                  onClick={e => {
                    e.stopPropagation();
                    if (onPathClick) {
                      onPathClick(idx.id);
                    }
                  }}
                >
                  {' '}
                  {index !== 0 && '->'} <span className={onPathClick ? styles.childName : ''}>{idx.name}</span>
                </span>
              );
            })}
        </Option>
      ))}
    </Select>
  );
});
