import React, { memo, useState, useCallback, useEffect, useMemo } from 'react';
import { AutoComplete, Input } from 'antd';
import { CloseOutlined } from '@ant-design/icons';

import useDebounce from '@/hooks/useDebounce';

import { RelatedTableItem } from '@/rematch/models/dataQuality';
import { searchAssetsService } from '@/services/glossary';

import styles from './RelatedTablesComp.less';

const { Search } = Input;

interface Props {
  selectedTables: RelatedTableItem[];
  defaultTableId: string;
  onChange: (v: RelatedTableItem[]) => void;
  dimension: string | null;
}

export default memo(function RelatedTablesComp({
  selectedTables,
  defaultTableId,
  onChange,
  dimension,
}: Props) {
  const [keyword, setKeyword] = useState('');

  const debounceKeyword = useDebounce(keyword, 300);

  const [tableOptions, setTableOptions] = useState<RelatedTableItem[]>([]);

  useEffect(() => {
    let ignore = false;
    const fetchFunc = async () => {
      const resp = await searchAssetsService(debounceKeyword);
      if (resp && resp.datasets && !ignore) {
        const showDatasetList = resp.datasets
          .filter(
            dataset => !selectedTables.map(i => i.id).includes(dataset.id),
          )
          .map(i => ({ ...i, value: `${i.name}-${i.datasource}`, key: i.id }));
        setTableOptions(showDatasetList);
      }
    };
    if (debounceKeyword) {
      fetchFunc();
    } else {
      setTableOptions([]);
    }
    return () => {
      ignore = true;
    };
  }, [debounceKeyword, selectedTables]);

  const handleSelectTable = useCallback(
    (_value: string, option: RelatedTableItem) => {
      const newSelectedTables = [...selectedTables, option].filter(i => i.id);
      onChange(newSelectedTables);
      setKeyword('');
    },
    [onChange, selectedTables],
  );

  const handleClickDelete = useCallback(
    (id: string) => {
      const newSelectedTables = selectedTables.filter(i => i.id !== id);
      onChange(newSelectedTables);
    },
    [onChange, selectedTables],
  );

  const newOrderSelectedTables = useMemo(() => {
    let newTable = [];
    const selfTable = selectedTables.find(i => i.id === defaultTableId);
    const otherTables = selectedTables.filter(i => i.id !== defaultTableId);
    if (selfTable) {
      newTable = [selfTable, ...otherTables];
    } else {
      newTable = selectedTables;
    }
    return newTable;
  }, [defaultTableId, selectedTables]);

  return (
    <div className={styles.relatedTablesComp}>
      {dimension === 'CUSTOMIZE' && (
        <AutoComplete
          options={tableOptions as any}
          onSelect={handleSelectTable as any}
          onChange={setKeyword}
          value={keyword}
        >
          <Search style={{ width: '100%' }} />
        </AutoComplete>
      )}
      <div className={styles.selectedTableList}>
        {newOrderSelectedTables.map(table => {
          if (table.id === defaultTableId) {
            return (
              <div key={table.id} className={styles.defaultSelectedTable}>
                {`${table.name} - ${table.datasource}`}
              </div>
            );
          }
          return (
            <div key={table.id} className={styles.selectedTable}>
              {`${table.name} - ${table.datasource}`}
              <CloseOutlined
                className={styles.closeIcon}
                onClick={() => handleClickDelete(table.id)}
              />
            </div>
          );
        })}
      </div>
    </div>
  );
});
