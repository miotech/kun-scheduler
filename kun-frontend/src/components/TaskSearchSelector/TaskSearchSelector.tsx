import React, { FunctionComponent, memo, useCallback, useEffect, useMemo, useState } from 'react';
import c from 'clsx';
import find from 'lodash/find';
import { useRequest } from 'ahooks';
import { Button, Select } from 'antd';
import { CloseOutlined, SearchOutlined } from '@ant-design/icons';
import { KunSpin } from '@/components/KunSpin';
import { searchTaskDefinition } from '@/services/data-development/task-definitions';

import './TaskSearchSelector.less';

export type ValueType = { value: string, label: string };

interface TaskSearchSelectorProps {
  // task definition ids
  value?: ValueType[];
  onChange?: (nextValue: ValueType[]) => any;
  className?: string;
}

export const TaskSearchSelector: FunctionComponent<TaskSearchSelectorProps> = memo((props) => {
  const { value = [], onChange, className } = props;
  const [ searchName, setSearchName ] = useState<string>('');

  const {
    data,
    loading,
    run: doSearch,
  } = useRequest(searchTaskDefinition, {
    throttleInterval: 1000,
    manual: true,
  });

  const fetchedTaskDefs = data ? data.records : [];

  useEffect(() => {
    if (searchName && searchName.length) {
      doSearch({
        name: searchName,
        pageNum: 1,
        pageSize: 50,
      });
    }
  }, [
    searchName,
  ]);

  const handleInputSearchChange = useCallback((value: string) => {
    setSearchName(value);
  }, [
    setSearchName,
  ]);

  const deselectTaskDefId = useCallback((deselectedId: string) => {
    const targetTaskDef = find(value, taskDef => taskDef.value === deselectedId);
    if (targetTaskDef) {
      onChange && onChange(value.filter(taskDef => taskDef.value !== deselectedId));
    }
  }, [
    value,
    onChange,
  ]);

  const listOfSelectedItems = useMemo(() => {
    return (value || []).map(taskDef => {
      return (
        <li
          className="taskdef-search-selector__selected-item"
          key={`${taskDef.value}`}
        >
          <div className="taskdef-search-selector__selected-item__name">
            <span data-label="task-definition-name">
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
  }, [
    value,
  ]);

  return (
    <div className={c('taskdef-search-selector', className)}>
      <div className="taskdef-search-selector__select-wrapper">
        <Select
          mode="multiple"
          showSearch
          className={c('taskdef-search-selector__select')}
          value={value}
          labelInValue
          onChange={onChange}
          notFoundContent={loading ? <KunSpin size="small" /> : null}
          // disable default filter
          filterOption={false}
          onSearch={handleInputSearchChange}
          suffixIcon={<SearchOutlined />}
        >
          {(fetchedTaskDefs || []).map(taskDef => (
            <Select.Option
              value={taskDef.id}
              key={`${taskDef.id}`}
            >
              {`${taskDef.name}`}
            </Select.Option>
          ))}
        </Select>
      </div>
      <ul className="taskdef-search-selector__selected-items-list">
        {listOfSelectedItems}
      </ul>
    </div>
  );
});
