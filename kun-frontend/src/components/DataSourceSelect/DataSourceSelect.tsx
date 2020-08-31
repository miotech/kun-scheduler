import React, {
  FunctionComponent,
  memo,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import c from 'classnames';
import find from 'lodash/find';
import { useRequest } from 'ahooks';
import { Button, Select } from 'antd';
import { CloseOutlined, SearchOutlined } from '@ant-design/icons';
import { KunSpin } from '@/components/KunSpin';
import { searchDataBasesService } from '@/services/dataSettings';

import './DataSourceSelect.less';
import { DataSource } from '@/definitions/DataSource.type';

export type ValueType = { value: string; label: string };

interface DataSourceSelectProps {
  // task definition ids
  value?: ValueType[];
  onChange?: (nextValue: ValueType[]) => any;
  className?: string;
}

export const DataSourceSelect: FunctionComponent<DataSourceSelectProps> = memo(
  props => {
    const { value = [], onChange, className } = props;
    const [searchName, setSearchName] = useState<string>('');

    const { data, loading, run: doSearch } = useRequest(
      searchDataBasesService,
      {
        throttleInterval: 1000,
        manual: true,
      },
    );

    const databases = data ? data.datasources : [];

    useEffect(() => {
      doSearch(searchName || '', {
        pageNumber: 1,
        pageSize: 50,
      });
    }, [doSearch, searchName]);

    const handleInputSearchChange = useCallback(
      (searchVal: string) => {
        setSearchName(searchVal);
      },
      [setSearchName],
    );

    const deselectTaskDefId = useCallback(
      (deselectedId: string) => {
        const targetTaskDef = find(
          value,
          taskDef => taskDef.value === deselectedId,
        );
        if (targetTaskDef && onChange) {
          onChange(value.filter(taskDef => taskDef.value !== deselectedId));
        }
      },
      [value, onChange],
    );

    const listOfSelectedItems = useMemo(() => {
      return (value || []).map(taskDef => {
        return (
          <li
            className="datasource-search-selector__selected-item"
            key={`${taskDef.value}`}
          >
            <div className="datasource-search-selector__selected-item__name">
              <span data-label="datasource-definition-name">
                {taskDef.label}
              </span>
              <Button
                size="small"
                data-label="close"
                type="link"
                icon={<CloseOutlined />}
                onClick={() => {
                  deselectTaskDefId(taskDef.value);
                }}
              />
            </div>
          </li>
        );
      });
    }, [deselectTaskDefId, value]);

    return (
      <div className={c('datasource-search-selector', className)}>
        <div className="taskdef-search-selector__select-wrapper">
          <Select
            mode="multiple"
            showSearch
            className={c('datasource-search-selector__select')}
            value={value}
            labelInValue
            onChange={onChange}
            notFoundContent={loading ? <KunSpin size="small" /> : null}
            // disable default filter
            filterOption={false}
            onSearch={handleInputSearchChange}
            suffixIcon={<SearchOutlined />}
          >
            {(databases || []).map((db: DataSource) => (
              <Select.Option value={db.id} key={`${db.id}`}>
                {`${db.name}`}
              </Select.Option>
            ))}
          </Select>
        </div>
        <ul className="datasource-search-selector__selected-items-list">
          {listOfSelectedItems}
        </ul>
      </div>
    );
  },
);
