import React, { memo, useState, useEffect, useCallback } from 'react';
import { AutoComplete, Tag } from 'antd';
import { Asset } from '@/rematch/models/glossary';
import useDebounce from '@/hooks/useDebounce';
import { searchAssetsService } from '@/services/glossary';
import { MinusOutlined } from '@ant-design/icons';

import styles from './AssetAutoSuggest.less';

const { Option } = AutoComplete;

interface Props {
  index: number;
  asset: Asset | null;
  onChange: (v: Asset, index: number) => void;
  onDelete: (index: number) => void;
  disabledIdList: string[];
}

export default memo(function AssetAutoSuggest({ asset, onChange, onDelete, index, disabledIdList }: Props) {
  const [keyword, setKeyword] = useState(asset?.name ?? '');
  const [assetList, setAssetList] = useState<Asset[]>(() => {
    if (asset) {
      return [asset];
    }
    return [];
  });

  const debounceKeyword = useDebounce(keyword, 300);

  useEffect(() => {
    let ignore = false;
    const search = async () => {
      if (debounceKeyword) {
        const resp = await searchAssetsService(debounceKeyword);
        if (resp && !ignore) {
          setAssetList(resp.datasets.filter(i => !disabledIdList.includes(i.id)) as Asset[]);
        }
      } else {
        setAssetList([]);
      }
    };
    search();
    return () => {
      ignore = true;
    };
  }, [debounceKeyword, disabledIdList]);

  const handleChange = useCallback(v => {
    setKeyword(v);
  }, []);

  const handleSelect = useCallback(
    (v, option) => {
      setKeyword(option.option.asset.name);
      onChange(option.option.asset, index);
    },
    [index, onChange],
  );

  const handleClickDelete = useCallback(() => {
    onDelete(index);
  }, [index, onDelete]);

  const options = assetList.map(item => ({
    value: `${item.name}-${item.database}-${item.datasource}`,
    asset: item,
    id: item.id,
  }));

  return (
    <div className={styles.container}>
      <AutoComplete
        className={styles.autoComplete}
        filterOption={false}
        onSelect={handleSelect}
        onChange={handleChange}
        value={keyword}
        // options={options}
      >
        {options.map(option => (
          <Option key={option.id} value={option.value} option={option}>
            <div className={styles.autoOption}>
              {option.value}{' '}
              {option.asset.database && (
                <Tag className={styles.tag} color="gold">
                  {option.asset.database}
                </Tag>
              )}
              {option.asset.datasource && (
                <Tag className={styles.tag} color="cyan">
                  {option.asset.datasource}
                </Tag>
              )}
            </div>
          </Option>
        ))}
      </AutoComplete>

      <div className={styles.deleteButton} onClick={handleClickDelete}>
        <MinusOutlined style={{ fontSize: 12 }} />
      </div>
    </div>
  );
});
