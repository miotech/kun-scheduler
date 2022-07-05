import React, { useState, useEffect, useRef } from 'react';
import { Input, Popover } from 'antd';
import { TableRecord, ErrorMessage, ShowStatus } from '@/definitions/ReferenceData.type';
import c from 'clsx';
import useI18n from '@/hooks/useI18n';
import styles from './EditTable.less';

interface EditableCellProps {
  findErrorLine: ErrorMessage;
  columnName: string;
  saveTableData: (record: TableRecord) => void;
  defaultValue: string;
  dataIndex: number;
  record: TableRecord;
}

export const EditTableCell: React.FC<EditableCellProps> = ({
  findErrorLine,
  columnName,
  saveTableData,
  defaultValue,
  dataIndex,
  record,
}) => {
  const [editing, setEditing] = useState(false);
  const [value, setValue] = useState(defaultValue);

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
    const newRecord = { ...record };
    newRecord[dataIndex] = value;
    saveTableData(newRecord);
  };
  const cellMessageList = findErrorLine?.cellMessageList;
  const findCellError = cellMessageList?.find(item => item.columnName === columnName);
  if (editing) {
    return (<Input ref={inputRef} value={value} onChange={e => setValue(e.target.value)} onPressEnter={save} onBlur={save} />);
  }

  if (findCellError) {
    return (
      <Popover placement="topLeft" content={findCellError?.message}>
        <div className={c(styles.editableCellValueWrap, findCellError ? styles.errorCell : '')} onClick={toggleEdit}>
          {defaultValue}
        </div>
      </Popover>
    );
  }
  return (
    <div className={c(styles.editableCellValueWrap)} onClick={toggleEdit}>
      {defaultValue}
    </div>
  );
};
interface RenderStatusProps {
  status: ShowStatus;
}

export const RenderStatus = (props: RenderStatusProps) => {
  const { status } = props;
  const t = useI18n();
  if (status === ShowStatus.HISTORY) {
    return (
      <span className={styles.status} style={{ color: '#FF6336' }}>
        <span style={{ backgroundColor: '#FF6336' }} className={styles.circle} />
        {t('dataDiscovery.referenceData.status.deactivated')}
      </span>
    );
  }
  if (status === ShowStatus.PUBLISHED) {
    return (
      <span className={styles.status} style={{ color: '#9BC655' }}>
        <span style={{ backgroundColor: '#9BC655' }} className={styles.circle} />
        {t('dataDiscovery.referenceData.status.released')}
      </span>
    );
  }
  if (status === ShowStatus.UNPUBLISHED) {
    return (
      <span className={styles.status} style={{ color: '#BD75AE' }}>
        <span style={{ backgroundColor: '#BD75AE' }} className={styles.circle} />
        {t('dataDiscovery.referenceData.status.saved')}
      </span>
    );
  }
  return (
    <span className={styles.status} style={{ color: '#B3C6D8' }}>
      <span style={{ backgroundColor: '#B3C6D8' }} className={styles.circle} />
      null
    </span>
  );
};
