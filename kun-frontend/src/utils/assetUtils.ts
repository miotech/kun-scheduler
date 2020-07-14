import { Asset } from '@/rematch/models/glossary';

const getAssetNameWithDatasource = (asset: Asset) => {
  if (asset.datasource) {
    return `${asset.name} - ${asset.datasource}`;
  }
  return asset.name;
};

export { getAssetNameWithDatasource };
