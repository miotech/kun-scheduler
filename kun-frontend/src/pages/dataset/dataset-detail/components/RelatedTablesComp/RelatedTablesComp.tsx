import React, { memo, useMemo } from 'react';

import { RelatedTableItem } from '@/rematch/models/dataQuality';

import styles from './RelatedTablesComp.less';

interface Props {
  selectedTables: RelatedTableItem[];
  defaultTableId: string | null;
}

export default memo(function RelatedTablesComp({
  selectedTables,
  defaultTableId,
}: Props) {
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
            </div>
          );
        })}
      </div>
    </div>
  );
});
