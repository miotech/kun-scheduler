import React, { useState, FunctionComponent, useMemo, useCallback, useEffect } from 'react';
import isNil from 'lodash/isNil';
import isFunction from 'lodash/isFunction';
import c from 'classnames';
import { AutoComplete, Select } from 'antd';
import { useRequest, useUpdateEffect } from 'ahooks';
import { searchDataBasesService } from '@/services/dataSettings';
import { searchDatasetsService } from '@/services/dataDiscovery';
import { KunSpin } from '@/components/KunSpin';

import './OutputDatasetField.less';
import useI18n from '@/hooks/useI18n';

interface OutputDataset {
  datasetName: string;
  datastoreId: string;
  // unused field
  definitionId?: string | null;
}

interface OwnProps {
  value?: OutputDataset;
  onChange?: (nextValue: OutputDataset) => any;
  className?: string;
}

type Props = OwnProps;

export const OutputDatasetField: FunctionComponent<Props> = (props) => {
  const {
    value: propsValue,
    onChange,
    className,
  } = props;

  const t = useI18n();

  const {
    data: databaseSearchData,
    loading: databaseSearchLoading,
    run: doDatabaseSearch,
  } = useRequest(searchDataBasesService, {
    throttleInterval: 1000,
    manual: true,
  });

  const {
    data: datasetSearchData,
    run: doDatasetSearch,
  } = useRequest(searchDatasetsService, {
    throttleInterval: 1000,
    manual: true,
  });

  // internal state
  const [ draftValue, setDraftValue ] = useState<OutputDataset>({
    datasetName: '',
    datastoreId: '',
  });
  const [ dataSourceSearchName, setDataSourceSearchName ] = useState<string>('');

  const value = isNil(propsValue) ? draftValue : propsValue;

  useEffect(() => {
    doDatabaseSearch(dataSourceSearchName, {
      pageNumber: 1,
      pageSize: 50,
    });
  }, [
    dataSourceSearchName,
  ]);

  useUpdateEffect(() => {
    doDatasetSearch({
      dsIdList: value?.datastoreId ? [value.datastoreId] : undefined,
      searchContent: value?.datasetName || undefined,
    }, {
      pageSize: 50,
      pageNumber: 1,
    });
  }, [
    value?.datastoreId,
    value?.datasetName,
  ]);

  const doChange = (nextValue: OutputDataset) => {
    if (isFunction(onChange)) {
      onChange(nextValue);
    } else {
      setDraftValue(nextValue);
    }
  };
  const handleDatasourceSearchChange = useCallback((dbSearchName: string) => {
    setDataSourceSearchName(dbSearchName);
  }, [
    setDataSourceSearchName,
  ]);

  /* render data source options */
  const dbOptions = useMemo(() => {
    return (databaseSearchData?.databases || []).map(db => (
      <Select.Option key={db.id} value={db.id}>
        {db.name}
      </Select.Option>
    ));
  }, [
    databaseSearchData,
  ]);

  const datasetNameOptions = useMemo(() => {
    if (!value?.datasetName) {
      return [];
    } else if (datasetSearchData?.datasets) {
      return datasetSearchData.datasets.map(ds => (
        // <Select.Option key={ds.id} value={ds.id}>
        //           {ds.name}
        //         </Select.Option>
        ({
          value: ds.name,
          label: ds.name,
          key: ds.id,
        })
      ));
    }
    // else
    return [];
  }, [
    value?.datasetName,
    datasetSearchData?.datasets,
  ]);

  return (
    <div className={c('output-dataset-field-container', className)}>
      <span className="output-dataset-field__datasource-select-wrapper">
        <Select
          placeholder={t('common.placeholder.datasource.select')}
          className="output-dataset-field__datasource-select"
          showSearch
          filterOption={false}
          notFoundContent={databaseSearchLoading ? <KunSpin size="small" /> : null}
          onSearch={handleDatasourceSearchChange}
          onChange={(selectedDataSourceId) => {
            doChange({
              ...value,
              datastoreId: selectedDataSourceId,
            });
          }}
          value={value?.datastoreId || undefined}
        >
          {dbOptions}
        </Select>
      </span>
      <span className="output-dataset-field__dataset-name-input-wrapper">
        <AutoComplete
          className="output-dataset-field__dataset-name-input"
          value={value?.datasetName}
          options={datasetNameOptions}
          onChange={(nextValue: string) => {
            doChange({
              ...value,
              datasetName: nextValue,
            });
          }}
          placeholder={t('common.placeholder.datasets.input')}
        />
      </span>
    </div>
  );
};
