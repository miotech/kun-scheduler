import React, { memo, useState, useEffect, useCallback } from 'react';
import { Input, Button } from 'antd';
import { EditOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';

import './DescriptionInput.less';

interface Props {
  value: string;
  onChange: (v: string) => void;
}

const { TextArea } = Input;

export default memo(function DescriptionInput({ value, onChange }: Props) {
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
    <div className="dataDiscoveryComponent-descriptionInput">
      <div className="dataDiscoveryComponent-baseItemTitle">
        <span style={{ marginRight: 8 }}>
          {t('dataDetail.baseItem.title.description')}
        </span>
        {!isEditing && (
          <EditOutlined
            className="dataDiscoveryComponent-baseItemTitle-editIcon"
            onClick={() => setIsEditing(true)}
          />
        )}
      </div>
      {isEditing ? (
        <div className="dataDiscoveryComponent-baseContent">
          <TextArea
            value={inputtingValue}
            allowClear
            onChange={e => {
              setInputtingValue(e.target.value);
            }}
          />
          <div className="dataDiscoveryComponent-buttonRow">
            <Button
              type="link"
              className="dataDiscoveryComponent-cancelButton"
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
        <div className="dataDiscoveryComponent-baseContent">{value}</div>
      )}
    </div>
  );
});
