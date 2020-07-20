import React, { memo, useState, useEffect, useCallback } from 'react';
import { AutoComplete } from 'antd';
import { Asset } from '@/rematch/models/glossary';
import useDebounce from '@/hooks/useDebounce';
import { searchAssetsService } from '@/services/glossary';
import { MinusOutlined } from '@ant-design/icons';
import { getAssetNameWithDatasource } from '@/utils/assetUtils';
import styles from './AssetAutoSuggest.less';

const { Option } = AutoComplete;

interface Props {
  index: number;
  asset: Asset | null;
  onChange: (v: Asset, index: number) => void;
  onDelete: (index: number) => void;
  disabledIdList: string[];
}

export default memo(function AssetAutoSuggest({
  asset,
  onChange,
  onDelete,
  index,
  disabledIdList,
}: Props) {
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
          setAssetList(
            resp.datasets.filter(
              i => !disabledIdList.includes(i.id),
            ) as Asset[],
          );
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
      setKeyword(v);
      onChange(option.option.asset, index);
    },
    [index, onChange],
  );

  const handleClickDelete = useCallback(() => {
    onDelete(index);
  }, [index, onDelete]);

  const options = assetList.map(item => ({
    value: getAssetNameWithDatasource(item),
    asset: item,
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
          <Option key={option.value} value={option.value} option={option}>
            <div className={styles.autoOption}>{option.value}</div>
          </Option>
        ))}
      </AutoComplete>

      <div className={styles.deleteButton} onClick={handleClickDelete}>
        <MinusOutlined style={{ fontSize: 12 }} />
      </div>
    </div>
  );
});
