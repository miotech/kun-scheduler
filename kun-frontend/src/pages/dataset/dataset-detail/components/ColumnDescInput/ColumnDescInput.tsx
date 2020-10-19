import React, { memo, useState, useEffect, useCallback } from 'react';
import { Input, Button } from 'antd';
import c from 'clsx';
import { EditOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';

import './ColumnDescInput.less';

interface Props {
  value: string;
  onChange: (v: string) => void;
  className?: string;
}

const { TextArea } = Input;

export default memo(function ColumnDescInput({
  value,
  onChange,
  className = '',
}: Props) {
  const t = useI18n();

  const [isEditing, setIsEditing] = useState(false);

  const [inputtingValue, setInputtingValue] = useState(value);

  useEffect(() => {
    setInputtingValue(value);
  }, [value]);

  const handleClickCancel = useCallback(() => {
    setIsEditing(false);
    setInputtingValue(value);
  }, [value]);

  const handleClickSave = useCallback(() => {
    setIsEditing(false);
    onChange(inputtingValue);
  }, [inputtingValue, onChange]);

  return (
    <div className={c('columnDescInput-descriptionInput', className)}>
      {!isEditing && (
        <EditOutlined
          className="columnDescInput-baseItemTitle-editIcon"
          onClick={() => setIsEditing(true)}
        />
      )}
      {isEditing ? (
        <div className="columnDescInput-baseContent">
          <TextArea
            value={inputtingValue}
            allowClear
            onChange={e => {
              setInputtingValue(e.target.value);
            }}
          />
          <div className="columnDescInput-buttonRow">
            <Button
              type="link"
              className="columnDescInput-cancelButton"
              onClick={handleClickCancel}
            >
              {t('common.button.cancel')}
            </Button>
            <Button type="link" onClick={handleClickSave}>
              {t('common.button.save')}
            </Button>
          </div>
        </div>
      ) : (
        <span className="columnDescInput-baseContent">{value}</span>
      )}
    </div>
  );
});
