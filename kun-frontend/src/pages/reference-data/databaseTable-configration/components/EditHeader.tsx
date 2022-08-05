/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useState, useEffect, useRef } from 'react';
import { Popover, Input, Select, Checkbox } from 'antd';
import { ColumnType, TableColumn, Column } from '@/definitions/ReferenceData.type';
import { DeleteOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';
import styles from './EditTable.less';

const { Option } = Select;

export const RenderFileName = (column: TableColumn, saveTableColumn: (column: TableColumn) => void) => {
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(column.name);

  const inputRef = useRef(null);
  useEffect(() => {
    if (editing) {
      inputRef.current!.focus();
    }
  }, [editing]);

  const toggleEdit = () => {
    setEditing(!editing);
  };

  const save = async () => {
    toggleEdit();
    saveTableColumn({ ...column, name });
  };
  const onChange = e => {
    setName(e.target.value);
  };

  let childNode = null;

  childNode = editing ? (
    <Input ref={inputRef} value={name} onChange={onChange} onPressEnter={save} onBlur={save} />
  ) : (
    <div className={styles.editableCellValueWrap} onClick={toggleEdit}>
      {column.name}
    </div>
  );

  return childNode;
};

export const DeleteContent = (dataIndex: number, deleteColumn: (index: number) => void) => {
  const t = useI18n();
  return (
    <div className={styles.deleteContent} onClick={() => deleteColumn(dataIndex)}>
      {t('dataDiscovery.referenceData.tableConfig.deleteField')} &nbsp; <DeleteOutlined />
    </div>
  );
};

export const RenderPrimaryKey = (
  column: TableColumn,
  saveTableColumn: (column: TableColumn) => void,
  editTable: boolean
) => {
  const save = (e: any) => {
    saveTableColumn({ ...column, primaryKey: e.target.checked });
  };
  return <Checkbox style={{ marginLeft: '12px' }} onChange={save} checked={column.primaryKey} disabled={!editTable} />;
};

export const RenderDataType = (column: TableColumn, saveTableColumn: (column: TableColumn) => void) => {
  const save = (value: ColumnType) => {
    saveTableColumn({ ...column, columnType: value });
  };

  return (
    <Select value={column.columnType} style={{ width: '100%' }} onChange={save} bordered={false}>
      {Object.keys(ColumnType).map(key => (
        <Option key={key} value={key}>
          {ColumnType[key as keyof typeof ColumnType]}
        </Option>
      ))}
    </Select>
  );
};

export const HeaderRow: React.FC = ({ ...props }, { saveTableColumn, deleteColumn, editTable, hasPublishedVersion, originColumns }) => {
  const t = useI18n();

  return (
    <>
      <div className={styles.headerRow} style={{ height: '49px' }}>
        {props.children?.map(child => {
          const { column } = child.props;
          const disabledEdit = hasPublishedVersion && originColumns.find((originColumn: Column) => originColumn.name === column.name);

          if (column.dataIndex === 'recordNumber') {
            return <div className={styles.columnLeft}> {t('dataDiscovery.referenceData.tableConfig.fieldName')}</div>;
          }
          if (editTable && !disabledEdit) {
            return (
              <Popover key={child.key} placement="top" content={() => DeleteContent(column.dataIndex, deleteColumn)}>
                <div key={child.key} style={{ width: column.width }} className={styles.tableHeaderEdit}>
                  {RenderFileName(column, saveTableColumn)}
                </div>
              </Popover>
            );
          }
          return (
            <div key={child.key} style={{ width: column.width }} className={styles.tableHeaderView}>
              {column.name}
            </div>
          );
        })}
      </div>
      <div className={styles.headerRow} style={{ height: '49px' }}>
        {props.children?.map(child => {
          const { column } = child.props;
          const disabledEdit = hasPublishedVersion && originColumns.find((originColumn: Column) => originColumn.name === column.name);

          if (column.dataIndex === 'recordNumber') {
            return <div className={styles.columnLeft}> {t('dataDiscovery.referenceData.tableConfig.dataType')}</div>;
          }
          if (editTable && !disabledEdit) {
            return (
              <div key={child.key} className={styles.tableHeaderEdit}>
                {RenderDataType(column, saveTableColumn)}
              </div>
            );
          }
          return (
            <div key={child.key} className={styles.tableHeaderView}>
              {column.columnType}
            </div>
          );
        })}
      </div>
      <div className={styles.headerRow}>
        {props.children?.map(child => {
          const { column } = child.props;
          if (column.dataIndex === 'recordNumber') {
            return <div className={styles.columnLeft}> {t('dataDiscovery.referenceData.tableConfig.primaryKey')}</div>;
          }
          return (
            <div key={child.key} className={styles.tableHeaderEdit}>
              {RenderPrimaryKey(column, saveTableColumn, editTable)}
            </div>
          );
        })}
      </div>
    </>
  );
};
