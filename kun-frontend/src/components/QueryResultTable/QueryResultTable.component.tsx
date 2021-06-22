import React, { memo, useCallback, useMemo } from 'react';
import ReactDataSheet from 'react-datasheet';
import { SheetRenderer } from '@/components/QueryResultTable/QueryResultTableSheetRenderer';

import { SQLQueryRow } from '@/definitions/QueryResult.type';
import 'react-datasheet/lib/react-datasheet.css';
import './QueryResultTable.less';

interface OwnProps {
  columnNames?: string[];
  data?: SQLQueryRow[];
}

type Props = OwnProps;

export const QueryResultTable: React.FC<Props> = memo(function QueryResultTable(props) {
  const {
    columnNames = [],
    data: propsData = [],
  } = props;

  const data: ReactDataSheet.Cell<any, string>[][] = useMemo(() => {
    const computedData: ReactDataSheet.Cell<any, string>[][] = [];
    for (let i = 0; i < propsData.length; i += 1) {
      computedData.push([]);
      for (let j = 0; j < propsData[i].length; j += 1) {
        // @ts-ignore
        computedData[i].push({ value: propsData[i][j], readOnly: true });
      }
    }
    return computedData;
  }, [propsData]);

  const valueRenderer = useCallback(function valueRenderer(cell, i, j) {
    if (cell.value != null) {
      return <span key={`cell-${i}-${j}`}>{cell.value}</span>;
    }
    // else
    return <span key={`cell-${i}-${j}-null`} className="cell-null" />;
  }, []);

  const dataRenderer = useCallback(function dataRenderer(cell) {
    return cell.value;
  }, []);

  const renderSheet = useCallback((otherProps: any) => {
    return (
      <SheetRenderer
        columns={(columnNames.map(colName => ({ label: colName })))}
        {...otherProps}
      />
    );
  }, [columnNames]);

  return (
    <div className="query-result-table">
      <ReactDataSheet
        className="query-result-table__datasheet"
        data={data}
        // @ts-ignore
        valueRenderer={valueRenderer}
        dataRenderer={dataRenderer}
        sheetRenderer={renderSheet}
        overflow="clip"
      />
    </div>
  );
});
