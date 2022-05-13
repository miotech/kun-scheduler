import React, { memo, useCallback, useMemo, useState } from 'react';
import { Link } from 'umi';
import { Tag } from 'antd';
import { FileTextOutlined, PlusOutlined, CloseOutlined } from '@ant-design/icons';
import { Asset } from '@/rematch/models/glossary';
import LineList from '@/components/LineList/LineList';
import useBackPath from '@/hooks/useBackPath';
import { getAssetNameWithDatasource } from '@/utils/assetUtils';
import AssetAutoSuggest from '../AssetAutoSuggest/AssetAutoSuggest';
import styles from './AssetList.less';

interface Props {
  isEditting: boolean;
  assetList: Asset[];
  onChange: (value: (Asset | null)[]) => void;
  onDeleteSingleAsset: (assetId: string) => void;
  onAddSingleAsset: (asset: Asset) => void;
}

export default memo(function AssetList({
  assetList,
  isEditting,
  onChange,
  onDeleteSingleAsset,
  onAddSingleAsset,
}: Props) {
  const { getBackPath } = useBackPath();
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

  const selectedIdList = useMemo(() => assetList.filter(i => !!i).map(i => i.id), [assetList]);

  const [isAdding, setIsAdding] = useState(false);

  const handleDeleteSingleAsset = useCallback(
    assetId => {
      onDeleteSingleAsset(assetId);
      setIsAdding(false);
    },
    [onDeleteSingleAsset],
  );

  const handleAddSingleAsset = useCallback(
    assetId => {
      onAddSingleAsset(assetId);
      setIsAdding(false);
    },
    [onAddSingleAsset],
  );

  if (!isEditting) {
    return (
      <>
        <LineList>
          {assetList
            .filter(asset => !!asset)
            .map(asset => (
              <div className={styles.childItem} key={asset!.id}>
                <FileTextOutlined />
                <div className={styles.right}>
                  <Link to={getBackPath(`/data-discovery/dataset/${asset!.id}`)}>
                    <span className={styles.name}>{getAssetNameWithDatasource(asset)}</span>
                  </Link>
                  <div style={{ marginLeft: 8 }}>
                    <span> {asset.owner?.[0]}</span>
                    {asset.database && (
                      <Tag className={styles.tag} color="gold">
                        {asset.database}
                      </Tag>
                    )}
                    {asset.datasource && (
                      <Tag className={styles.tag} color="cyan">
                        {asset.datasource}
                      </Tag>
                    )}
                    <div>{asset.description}</div>
                  </div>
                </div>
                <CloseOutlined style={{ marginLeft: 4 }} onClick={() => handleDeleteSingleAsset(asset.id)} />
              </div>
            ))}
          {isAdding && (
            <AssetAutoSuggest
              index={1}
              asset={null}
              onChange={handleAddSingleAsset}
              onDelete={() => setIsAdding(false)}
              disabledIdList={selectedIdList}
            />
          )}
        </LineList>
        <div className={styles.addButtonCon}>
          <div
            className={styles.addButton}
            onClick={() => {
              setIsAdding(true);
            }}
          >
            <PlusOutlined />
          </div>
        </div>
      </>
    );
  }
  return (
    <>
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
      </LineList>
      <div className={styles.addButtonCon}>
        <div className={styles.addButton} onClick={handleClickAdd}>
          <PlusOutlined />
        </div>
      </div>
    </>
  );
});
