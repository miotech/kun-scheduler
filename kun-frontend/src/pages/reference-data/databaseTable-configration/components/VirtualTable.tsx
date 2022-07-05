import { Table, Popover } from 'antd';
import ResizeObserver from 'rc-resize-observer';
import React, { useEffect, useRef, memo, useState } from 'react';
import { VariableSizeGrid as Grid } from 'react-window';
import {
  ErrorMessage,

} from '@/definitions/ReferenceData.type';
import c from 'clsx';
import { DeleteOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';
import styles from "./VirtualTable.less";
import { EditTableCell } from './EditTable';


export const DeleteContent = (rowIndex: number, deleteRow: (index: number) => void) => {
  const t = useI18n();
  return (
    <div className={styles.deleteContent} onClick={() => deleteRow(rowIndex)}>
      {t('dataDiscovery.referenceData.tableConfig.deleteData')} &nbsp; <DeleteOutlined />
    </div>
  );
};


const VirtualTable = memo((props: Parameters<typeof Table>[0]) => {
  const { columns, scroll, tableHeader, isEditing, saveTableData, deleteRow, errorMessages } = props;
  const [tableWidth, setTableWidth] = useState(0);

  const widthColumnCount = columns!.filter(({ width }) => !width).length;
  const mergedColumns = columns!.map(column => {
    if (column.width) {
      return column;
    }

    return {
      ...column,
      width: Math.floor(tableWidth / widthColumnCount),
    };
  });

  const gridRef = useRef<any>();
  const [connectObject] = useState<any>(() => {
    const obj = {};
    Object.defineProperty(obj, 'scrollLeft', {
      get: () => {
        if (gridRef.current) {
          return gridRef.current?.state?.scrollLeft;
        }
        return null;
      },
      set: (scrollLeft: number) => {
        if (gridRef.current) {
          gridRef.current.scrollTo({ scrollLeft });
        }
      },
    });

    return obj;
  });

  const resetVirtualGrid = () => {
    gridRef.current.resetAfterIndices({
      columnIndex: 0,
      shouldForceUpdate: true,
    });
  };

  useEffect(() => resetVirtualGrid, [tableWidth]);

  const renderVirtualList = (rawData: object[], { scrollbarSize, ref, onScroll }: any) => {
    ref.current = connectObject;
    const totalHeight = rawData.length * 49;

    return (
      <Grid
        ref={gridRef}
        className="virtual-grid"
        columnCount={mergedColumns.length}
        columnWidth={(index: number) => {
          const { width } = mergedColumns[index];
          return totalHeight > scroll!.y! && index === mergedColumns.length - 1
            ? (width as number) - scrollbarSize - 1
            : (width as number);
        }}
        height={scroll!.y as number}
        rowCount={rawData.length}
        rowHeight={() => 49}
        width={tableWidth}
        onScroll={({ scrollLeft }: { scrollLeft: number }) => {
          onScroll({ scrollLeft });
        }}
      >
        {({
          columnIndex,
          rowIndex,
          style,
        }: {
          columnIndex: number;
          rowIndex: number;
          style: React.CSSProperties;
        }) => {
          if (isEditing) {
            const findErrorLine = errorMessages.find((err: ErrorMessage) => parseInt(err.lineNumber, 10) === rowIndex + 1);
            if (columnIndex === 0) {
              if (findErrorLine) {
                const { rowMessage, cellMessageList } = findErrorLine;
                let popoverContent = rowMessage;
                if (cellMessageList) {
                  popoverContent = JSON.stringify(cellMessageList);
                }

                return (<div
                  className={c(styles.virtualTableCell, rowMessage ? styles.errorCell : '')}
                  style={style}
                >
                  <Popover placement="topLeft" content={popoverContent}>
                    <div className={c(styles.editableCellValueWrap, styles.errorLine)}>
                      {(rawData[rowIndex] as any)[(mergedColumns as any)[columnIndex].dataIndex]}
                    </div>
                  </Popover>
                </div>);
              }
              return (<div
                className={styles.virtualTableCell}
                style={style}
              >
                <Popover placement="bottom" content={() => DeleteContent(rowIndex, deleteRow)}>
                  <div className={styles.editableCellValueWrap}>
                    {(rawData[rowIndex] as any)[(mergedColumns as any)[columnIndex].dataIndex]}
                  </div>
                </Popover>
              </div>);
            }

            if (findErrorLine?.rowMessage) {
              return (
                <div
                  className={c(styles.virtualTableCell, styles.errorCell)}
                  style={style}
                >
                  < EditTableCell findErrorLine={findErrorLine} columnName={(mergedColumns as any)[columnIndex].name} saveTableData={saveTableData} defaultValue={(rawData[rowIndex] as any)[(mergedColumns as any)[columnIndex].dataIndex]} dataIndex={[(mergedColumns as any)[columnIndex].dataIndex]} record={rawData[rowIndex]} />
                </div>
              );
            }
            return (
              <div
                className={styles.virtualTableCell}
                style={style}
              >
                < EditTableCell findErrorLine={findErrorLine} columnName={(mergedColumns as any)[columnIndex].name} saveTableData={saveTableData} defaultValue={(rawData[rowIndex] as any)[(mergedColumns as any)[columnIndex].dataIndex]} dataIndex={[(mergedColumns as any)[columnIndex].dataIndex]} record={rawData[rowIndex]} />
              </div>
            );
          }
          return (
            <div
              className={styles.virtualTableCell}
              style={style}
            >
              <div className={styles.editableCellValueWrap}>
                {(rawData[rowIndex] as any)[(mergedColumns as any)[columnIndex].dataIndex]}
              </div>
            </div>
          );

        }}
      </Grid >
    );
  };

  return (
    <ResizeObserver
      onResize={({ width }) => {
        setTableWidth(width);
      }}
    >
      <Table
        {...props}
        className="virtual-table"
        columns={mergedColumns}
        pagination={false}
        components={{
          header: tableHeader,
          body: (data, info) => renderVirtualList(data, info),
        }}
      />
    </ResizeObserver>
  );
});

export default VirtualTable;