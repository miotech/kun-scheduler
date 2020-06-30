import React, { memo, useCallback, useMemo } from 'react';
import { Link } from 'umi';
import { FileTextOutlined, PlusOutlined } from '@ant-design/icons';
import { Asset } from '@/rematch/models/glossary';
import LineList from '@/components/LineList/LineList';
import AssetAutoSuggest from '../AssetAutoSuggest/AssetAutoSuggest';
import styles from './AssetList.less';

interface Props {
  isEditting: boolean;
  assetList: Asset[];
  onChange: (value: (Asset | null)[]) => void;
}

export default memo(function AssetList({
  assetList,
  isEditting,
  onChange,
}: Props) {
  const handleChange = useCallback(
    (newAsset: Asset, index: number) => {
      const newAssetList = assetList.map((asset, i) => {
        if (i !== index) {
          return asset;
        }
        return newAsset;
      });
      onChange(newAssetList);
    },
    [assetList, onChange],
  );

  const handleDelete = useCallback(
    (index: number) => {
      const newAssetList = assetList.filter((_asset, i) => i !== index);
      onChange(newAssetList);
    },
    [assetList, onChange],
  );

  const handleClickAdd = useCallback(() => {
    onChange([...assetList, null]);
  }, [assetList, onChange]);

  const selectedIdList = useMemo(
    () => assetList.filter(i => !!i).map(i => i.id),
    [assetList],
  );

  if (!isEditting) {
    return (
      <LineList>
        {assetList
          .filter(asset => !!asset)
          .map(asset => (
            <div className={styles.childItem} key={asset!.id}>
              <FileTextOutlined />
              <Link to={`/data-discovery/dataset/${asset!.id}`}>
                <span className={styles.name}>{asset!.name}</span>
              </Link>
            </div>
          ))}
      </LineList>
    );
  }
  return (
    <LineList>
      {assetList.map((asset, index) => (
        <AssetAutoSuggest
          index={index}
          key={asset?.id || index}
          asset={asset}
          onChange={handleChange}
          onDelete={handleDelete}
          disabledIdList={selectedIdList}
        />
      ))}
      <div className={styles.addButton} onClick={handleClickAdd}>
        <PlusOutlined />
      </div>
    </LineList>
  );
});
