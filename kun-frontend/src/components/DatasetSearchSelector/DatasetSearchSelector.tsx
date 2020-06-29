import React, { FunctionComponent, memo, useCallback, useEffect, useMemo, useState } from 'react';
import c from 'classnames';
import find from 'lodash/find';
import { useRequest } from 'ahooks';
import { Button, Select } from 'antd';
import { CloseOutlined, SearchOutlined } from '@ant-design/icons';
import { KunSpin } from '@/components/KunSpin';
import { LogUtils } from '@/utils/logUtils';

import { DatasetTaskDefSummary } from '@/definitions/DatasetTaskDefSummary.type';
import { searchDatasetAndRelatedTaskDefs } from '@/services/data-development/dataset';

import './DatasetSearchSelector.less';

interface DatasetSearchSelectorProps {
  value?: DatasetTaskDefSummary[];
  onChange?: (nextValue: DatasetTaskDefSummary[]) => any;
  className?: string;
}

const logger = LogUtils.getLoggers('DatasetSearchSelector');

export const DatasetSearchSelector: FunctionComponent<DatasetSearchSelectorProps> = memo((props) => {
  const { value = [], onChange, className } = props;
  // const [ dataset, setDataset ] = useState<DatasetTaskDefSummary[]>([]);
  const [ searchName, setSearchName ] = useState<string>('');
  const [ selectedDatasets, setSelectedDatasets ] = useState<string[]>(value.map(item => `${item.datastoreId}:::${item.name}`));

  const {
    data: fetchedDatasets,
    loading,
    run: doSearch,
  } = useRequest(searchDatasetAndRelatedTaskDefs, {
    throttleInterval: 1000,
    manual: true,
  });

  useEffect(() => {
    if (searchName && searchName.length) {
      doSearch({
        name: searchName,
      });
    }
  }, [
    searchName,
  ]);

  useEffect(() => {
    if (value) {
      setSelectedDatasets(value.map(item => `${item.datastoreId}:::${item.name}`));
    }
  }, [
    value,
  ]);

  const handleInputSearchChange = useCallback((searchVal: string) => {
    setSearchName(searchVal);
  }, [
    setSearchName,
  ]);

  const deselectDatastoreId = useCallback((deselectedId: string) => {
    if (selectedDatasets.indexOf(deselectedId) >= 0) {
      setSelectedDatasets(selectedDatasets.filter(item => item !== deselectedId));
      if (onChange) {
        onChange(value.filter(dataset => `${dataset.datastoreId}:::${dataset.name}` !== deselectedId));
      }
    }
  }, [
    value,
    onChange,
    selectedDatasets,
    setSelectedDatasets,
  ]);

  const onSelectCallback = useCallback((selectedStoreIdWithName: string) => {
    const parts = (selectedStoreIdWithName || '').split(':::');
    if (!parts.length) {
      logger.debug('Invalid argument selectedStoreIdWithName:', selectedStoreIdWithName);
      return;
    }
    // else
    const [ selectedStoreId, name ] = parts;
    const targetDataset = find(fetchedDatasets, dataset => (dataset.datastoreId === selectedStoreId) && (dataset.name === name));
    if (!targetDataset) {
      logger.debug('NOT FOUND:', selectedStoreId);
      return;
    }
    // if pushing new item
    if (selectedDatasets.indexOf(`${targetDataset.datastoreId}:::${targetDataset.name}`) === -1) {
      setSelectedDatasets(selectedDatasets.concat(`${targetDataset.datastoreId}:::${targetDataset.name}`));
      if (onChange) {
        onChange(value.concat(targetDataset));
      }
    } else {
      // if removing an item
      deselectDatastoreId(`${targetDataset.datastoreId}:::${targetDataset.name}`);
    }
  }, [
    fetchedDatasets,
    selectedDatasets,
    onChange,
    value,
    deselectDatastoreId,
  ]);

  const listOfSelectedItems = useMemo(() => {
    return (value || []).map(item => {
      return (
        <li
          className="dataset-search-selector__selected-item"
          key={`${item.datastoreId}-${item.name}`}
        >
          <div className="dataset-search-selector__selected-item__name">
            <span data-label="name">{item.name}</span>
            <Button
              size="small"
              data-label="close"
              type="link"
              icon={<CloseOutlined />}
              onClick={() => {
                deselectDatastoreId(`${item.datastoreId}:::${item.name}`);
              }}
            />
          </div>
          <ul className="dataset-search-selector__related-task-defs">
            {(item.taskDefinitions || []).map(taskDef => (
              <li className="dataset-search-selector__related-task-def-item" key={taskDef.id}>
                {taskDef.name}
              </li>
            ))}
          </ul>
        </li>
      );
    });
  }, [
    deselectDatastoreId,
    value,
  ]);

  return (
    <div className={c('dataset-search-selector', className)}>
      <div className="dataset-search-selector__select-wrapper">
        <Select
          mode="multiple"
          showSearch
          className={c('dataset-search-selector__select')}
          value={selectedDatasets}
          onSelect={onSelectCallback}
          onDeselect={onSelectCallback}
          notFoundContent={loading ? <KunSpin size="small" /> : null}
          // disable default filter
          filterOption={false}
          onSearch={handleInputSearchChange}
          suffixIcon={<SearchOutlined />}
        >
          {(fetchedDatasets || []).map(opt => (
            <Select.Option
              value={`${opt.datastoreId}:::${opt.name}`}
              key={`${opt.datastoreId}:::${opt.name}`}
            >
              {`${opt.name}`}
            </Select.Option>
          ))}
        </Select>
      </div>
      <ul className="dataset-search-selector__selected-items-list">
        {listOfSelectedItems}
      </ul>
    </div>
  );
});
