import React, { memo, useEffect, useState } from 'react';
import LineList from '@/components/LineList/LineList';
import { PlusOutlined, MinusOutlined, CloseOutlined, UserOutlined } from '@ant-design/icons';
import { UserSelect } from '@/components/UserSelect';
import { addGlossaryEditor, removeGlossaryEditor, queryGlossaryEditor } from '@/services/glossary';
import { useRequest } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import { Popconfirm } from 'antd';
import styles from './Editor.less';

interface Props {
  id: string | undefined;
  hasPermission: boolean | undefined;
}
export default memo(function Editor({ id, hasPermission }: Props) {
  const t = useI18n();
  const I18n = 'glossary.userRole';
  const [isAdding, setIsAdding] = useState(false);
  const queryGlossaryEditorRequest = useRequest(queryGlossaryEditor, {
    manual: true,
  });

  const handleUserChange = async (value: any) => {
    const params = {
      userName: value as string,
      id: id as string,
    };
    const res = await addGlossaryEditor(params);
    if (res) {
      setIsAdding(false);
      queryGlossaryEditorRequest.run(id as string);
    }
  };

  const deletEditor = async (value: string) => {
    const params = {
      userName: value,
      id: id as string,
    };
    const res = await removeGlossaryEditor(params);
    if (res) {
      queryGlossaryEditorRequest.run(id as string);
    }
  };
  useEffect(() => {
    if (id) {
      queryGlossaryEditorRequest.run(id);
    }
  }, [id]);

  return (
    <div className={styles.inputBlock}>
      <div className={styles.label}>
        {t(`${I18n}.title`)}{' '}
        {(queryGlossaryEditorRequest?.data || []).length > 0 && (
          <span style={{ marginLeft: 4 }}>({(queryGlossaryEditorRequest?.data || []).length})</span>
        )}
      </div>
      <div>
        <LineList>
          {queryGlossaryEditorRequest?.data?.map(editor => (
            <div className={styles.childItem} key={editor}>
              <UserOutlined />
              <div className={styles.user}>{editor} </div>
              {hasPermission && (
                <Popconfirm
                  title={t(`${I18n}.delete`)}
                  onConfirm={() => deletEditor(editor)}
                  okText={t('common.button.confirm')}
                  cancelText={t('common.button.cancel')}
                >
                  <CloseOutlined style={{ marginLeft: 4 }} />
                </Popconfirm>
              )}
            </div>
          ))}
          {isAdding && (
            <div className={styles.childItem}>
              <UserSelect
                style={{ width: '250px' }}
                onChange={handleUserChange}
                allowClear
                placeholder={t(`${I18n}.placeholder`)}
                className="full-width"
              />
              <div className={styles.deleteButton} onClick={() => setIsAdding(false)}>
                <MinusOutlined style={{ fontSize: 12 }} />
              </div>
            </div>
          )}
        </LineList>
        {hasPermission && (
          <div className={styles.addButtonCon}>
            <div className={styles.addButton} onClick={() => setIsAdding(true)}>
              <PlusOutlined />
            </div>
          </div>
        )}
      </div>
    </div>
  );
});
