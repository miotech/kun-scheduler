import React, { useState, useMemo, useEffect, useCallback, forwardRef, useImperativeHandle } from 'react';
import { Col, Input, Row, Form, Tag, Select, Modal } from 'antd';
import {
  Data,
  Column,
  TableColumn,
  TableRecord,
  ColumnType,
  TableConfigDetail,
  ErrorMessage,
  DataBase,
} from '@/definitions/ReferenceData.type';
import { FormInstance } from 'antd/es/form/Form';
import AutosuggestInput from '@/pages/glossary/components/AutosuggestInput/AutosuggestInput';
import { UserSelect } from '@/components/UserSelect';
import { GlossaryChild } from '@/rematch/models/glossary';
import { getDatabases } from '@/services/reference-data/referenceData';
import { useRequest, useMount } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { UserState } from '@/rematch/models/user';
import { HeaderRow } from './EditHeader';
import { RenderStatus } from './EditTable';
import styles from './EditTable.less';
import VirtualTable from './VirtualTable';

const { TextArea } = Input;
const { Option } = Select;
interface Props {
  form?: FormInstance;
  glossaryListOptions: GlossaryChild[];
  isEditing: boolean;
  tableConfigDetail?: TableConfigDetail;
  originColumns: Column[];
  originData: Data[];
  primaryKeys: string[];
  errorMessages?: ErrorMessage[];
  validateTable?: (data: TableRecord[], column: TableColumn[]) => void;
}

const transOriDataToTabData = (data: Data[]) => {
  const newData = data?.map((item: Data) => {
    const obj = {
      recordNumber: item.recordNumber,
    } as TableRecord;
    item.values.forEach((value: string, idx: number) => {
      obj[idx] = value;
    });
    return obj;
  });
  return newData;
};

const transOriColumnsToTabColums = (originColumns: Column[], primaryKeys: string[]) => {
  const newColumns = originColumns.map(column => {
    const primaryKey = !!primaryKeys.includes(column.name);
    return {
      ...column,
      dataIndex: column.index,
      primaryKey,
      width: 200,
    };
  });
  newColumns.unshift({
    dataIndex: 'recordNumber',
    width: 200,
  });
  return newColumns;
};
const ReferRenceTable: React.FC<Props> = forwardRef((props, ref) => {
  const {
    originColumns,
    originData,
    primaryKeys,
    form,
    glossaryListOptions,
    isEditing,
    tableConfigDetail,
    errorMessages,
    validateTable,
  } = props;
  const [tableData, setTableData] = useState<TableRecord[]>([]);
  const [tableColumns, setTableColumns] = useState<TableColumn[]>([]);
  const [currentErrorMessages, setCurrentErrorMessages] = useState<ErrorMessage[] | undefined>([]);
  const { selector } = useRedux<UserState>(state => state.user);

  const t = useI18n();
  const i18n = 'dataDiscovery.referenceData.tableConfig';

  useImperativeHandle(ref, () => ({
    tableData,
    tableColumns,
  }));

  const { data: databases, run: queryDatabases } = useRequest(getDatabases, {
    manual: true,
  });

  useMount(() => {
    queryDatabases();
  });

  useEffect(() => {
    const newColumns = transOriColumnsToTabColums(originColumns, primaryKeys);
    setTableColumns(newColumns);
  }, [originColumns, primaryKeys]);

  useEffect(() => {
    setCurrentErrorMessages(errorMessages);
  }, [errorMessages]);

  useEffect(() => {
    const newTableData = transOriDataToTabData(originData);
    setTableData(newTableData);
  }, [originData]);

  const saveTableData = (row: TableRecord) => {
    if (currentErrorMessages?.length) {
      const findIndex = currentErrorMessages.findIndex(item => item.lineNumber === row.recordNumber);
      if (findIndex > -1) {
        const newErrorMessages = [...currentErrorMessages];
        newErrorMessages.splice(findIndex, 1);
        setCurrentErrorMessages(newErrorMessages);
      }
    }
    const newData = [...tableData];
    const index = newData.findIndex(item => row.recordNumber === item.recordNumber);
    const item = newData[index];
    newData.splice(index, 1, {
      ...item,
      ...row,
    });
    setTableData(newData);
  };

  const saveTableColumn = useCallback(
    (column: TableColumn) => {
      const newColumn = { ...column };
      const newTableColumns = [...tableColumns];
      const index = tableColumns.findIndex(item => newColumn.index === item.index);
      newTableColumns.splice(index, 1, newColumn);
      setTableColumns(newTableColumns);
    },
    [tableColumns, setTableColumns],
  );

  const deleteColumn = useCallback(
    dataIndex => {
      Modal.confirm({
        title: t('dataDiscovery.referenceData.notice.deleteField'),
        async onOk() {
          const newTableColumns = [...tableColumns];
          const findIndex = newTableColumns.findIndex(item => item.dataIndex === dataIndex);
          newTableColumns.splice(findIndex, 1);
          const newTableData = tableData.map(item => {
            const newData = { ...item };
            delete newData[dataIndex];
            return newData;
          });
          setTableColumns(newTableColumns);
          setTableData(newTableData);
          if (validateTable) {
            await validateTable(newTableData, newTableColumns);
          }
        },
      });
    },
    [tableColumns, tableData, setTableData, setTableColumns, validateTable, t],
  );

  const deleteRow = useCallback(
    rowIndex => {
      Modal.confirm({
        title: t('dataDiscovery.referenceData.notice.deleteData'),
        async onOk() {
          const newTableData = [...tableData];
          newTableData.splice(rowIndex, 1);
          setTableData(newTableData);
          if (validateTable) {
            await validateTable(newTableData, tableColumns);
          }
        },
      });
    },
    [tableData, setTableData, validateTable, tableColumns, t],
  );

  const addNewColumn = useCallback(() => {
    const newIndex = tableColumns[tableColumns.length - 1].index + 1;
    const newColumn = {
      name: '',
      index: newIndex,
      columnType: ColumnType.string,
      dataIndex: newIndex,
      primaryKey: false,
      width: 200,
      editable: true,
      onCell: (record: TableRecord) => ({
        record,
        dataIndex: newIndex,
        name: '',
        primaryKey: false,
      }),
    } as TableColumn;
    const newColumns = [...tableColumns, newColumn];
    const newTableData = tableData.map(item => {
      const newData = { ...item };
      newData[newIndex] = null;
      return newData;
    });
    setTableData(newTableData);
    setTableColumns(newColumns);
  }, [tableColumns, tableData, setTableData, setTableColumns]);

  const addTableData = useCallback(() => {
    const newData: TableRecord = {
      recordNumber: (parseInt(tableData[tableData.length - 1].recordNumber, 10) + 1).toString(),
    };
    tableColumns.forEach((column: TableColumn) => {
      if (column.index) {
        newData[column.index] = null;
      }
    });
    const newTableData = [...tableData, newData] as TableRecord[];
    setTableData(newTableData);
  }, [tableData, tableColumns, setTableData]);

  const tableHeader = {
    row: (row: TableRecord) =>
      HeaderRow(row, {
        saveTableColumn,
        deleteColumn,
        editTable: isEditing,
        hasPublishedVersion: tableConfigDetail && !tableConfigDetail?.enableEditName,
        originColumns,
      }),
  };
  const initFormValue = useMemo(() => {
    return {
      ownerList: selector?.username ? [selector?.username] : null,
      databaseName: 'ref',
    };
  }, [selector]);
  return (
    <div className={styles.tableConfigration}>
      <Form form={form} initialValues={initFormValue} name="control-hooks">
        <Row>
          <Col span={6}>
            <div className={styles.label}>
              <span style={{ color: 'red' }}>*&nbsp;</span>
              {t(`${i18n}.referenceTableName`)}{' '}
            </div>
            <div className={styles.value}>
              {isEditing && (
                <Form.Item name="tableName" rules={[{ required: true }]}>
                  <Input disabled={tableConfigDetail && !tableConfigDetail?.enableEditName} />
                </Form.Item>
              )}
              {!isEditing && <div className={styles.showValue}>{tableConfigDetail?.tableName}</div>}
            </div>
          </Col>

          <Col span={6} offset={3}>
            <div className={styles.label}>{t(`${i18n}.status`)}</div>
            <div className={styles.value}>
              {tableConfigDetail?.showStatus ? <RenderStatus status={tableConfigDetail?.showStatus} /> : ''}
            </div>
          </Col>
        </Row>
        <Row>
          <Col span={6}>
            <div className={styles.label}>
              <span style={{ color: 'red' }}>*&nbsp;</span>
              {t(`${i18n}.database`)}{' '}
            </div>
            <div className={styles.value}>
              {isEditing && (
                <Form.Item name="databaseName" rules={[{ required: true }]}>
                  <Select showSearch disabled={tableConfigDetail && !tableConfigDetail?.enableEditName}>
                    {databases?.map((database: DataBase) => (
                      <Option value={database.name}>{database.name}</Option>
                    ))}
                  </Select>
                </Form.Item>
              )}
              {!isEditing && <div className={styles.showValue}>{tableConfigDetail?.databaseName}</div>}
            </div>
          </Col>
          <Col span={6} offset={3}>
            <div className={styles.label}>{t(`${i18n}.glossary`)}</div>
            <div className={styles.value}>
              {isEditing && (
                <Form.Item name="glossaryList">
                  <AutosuggestInput initOptions={glossaryListOptions} showPath mode="multiple" />
                </Form.Item>
              )}

              {!isEditing && (
                <div className={styles.showValue}>
                  {glossaryListOptions.map((glossary: GlossaryChild) => (
                    <Tag>{glossary.name}</Tag>
                  ))}
                </div>
              )}
            </div>
          </Col>
          <Col span={6} offset={3}>
            <div className={styles.label}>{t(`${i18n}.owner`)}</div>
            <div className={styles.value}>
              {isEditing && (
                <Form.Item name="ownerList">
                  <UserSelect mode="multiple" />
                </Form.Item>
              )}
              {!isEditing && (
                <div className={styles.showValue}>
                  {tableConfigDetail?.ownerList?.map((name: string) => (
                    <Tag>{name}</Tag>
                  ))}
                </div>
              )}
            </div>
          </Col>
        </Row>
        <Row>
          <Col span={24}>
            <div className={styles.label}>{t(`${i18n}.description`)}</div>
            <div className={styles.value}>
              {isEditing && (
                <Form.Item name="versionDescription">
                  <TextArea rows={4} />
                </Form.Item>
              )}
              {!isEditing && <div className={styles.showValue}>{tableConfigDetail?.versionDescription}</div>}
            </div>
          </Col>
        </Row>
      </Form>
      <div className={styles.tableContent}>
        <div className={styles.center} style={{ width: `${201 * tableColumns.length}px` }}>
          {/* <Table
            components={{
              // header: {
              //   row: (row: TableRecord) => HeaderRow(row, { saveTableColumn, deleteColumn, editTable: isEditing }),
              // },
              body: renderVirtualList,
            }}
            rowKey="recordNumber"
            size="small"
            bordered
            // dataSource={tableData}
            columns={tableColumns as ColumnTypes}
            className={styles.editTable}
            // scroll={{ y: 200 }}
            pagination={false}
          /> */}
          <VirtualTable
            columns={tableColumns}
            errorMessages={currentErrorMessages}
            saveTableData={saveTableData}
            deleteRow={deleteRow}
            tableHeader={tableHeader}
            dataSource={tableData}
            scroll={{ y: 1200 }}
            isEditing={isEditing}
          />
        </div>

        {isEditing && (
          <div className={styles.right}>
            <div className={styles.addFiled} onClick={addNewColumn}>
              {' '}
              + {t(`${i18n}.addField`)}{' '}
            </div>
          </div>
        )}
      </div>
      {isEditing && (
        <div className={styles.addData} onClick={addTableData}>
          + {t(`${i18n}.addData`)}
        </div>
      )}
    </div>
  );
});

export default ReferRenceTable;
