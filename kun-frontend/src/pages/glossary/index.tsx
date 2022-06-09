import React, { useCallback, useRef, useMemo, useState } from 'react';
import { Button } from 'antd';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import Card from '@/components/Card/Card';
import { useRequest } from 'ahooks';

import { queryGlossaryRole } from '@/services/glossary';
import { Operation, GlossaryDisplayType } from '@/definitions/Glossary.type';
import AutosuggestInput from './components/AutosuggestInput/AutosuggestInput';
import GlossaryTree from './components/GlossaryTree/GlossaryTree';
import GlossaryListView from './glossary-list';
import { DisplayTypeSwitch } from './components/DisplayTypeSwitch';
import styles from './index.less';

export default function Glossary() {
  const t = useI18n();
  const glossaryDisplayTypeStorage = localStorage.getItem('glossaryDisplayType') as GlossaryDisplayType;
  const [glossaryDisplayType, setGlossaryDisplayType] = useState<GlossaryDisplayType>(
    glossaryDisplayTypeStorage || GlossaryDisplayType.RELATION,
  );

  const childrenRef = useRef(null);
  const { selector } = useRedux<any>(state => state.glossary);

  const queryGlossaryRoleRequest = useRequest(queryGlossaryRole);
  const handleClickCreate = useCallback(() => {
    childrenRef.current.create();
  }, []);

  const setCurrentId = useCallback((id: string) => {
    childrenRef.current.setCurrentId(id);
  }, []);

  const GLossaryRole = useMemo(() => {
    return queryGlossaryRoleRequest?.data?.operations;
  }, [queryGlossaryRoleRequest.data]);

  const changeDisplayType = (type: GlossaryDisplayType) => {
    setGlossaryDisplayType(type);
    localStorage.setItem('glossaryDisplayType', type);
  };

  return (
    <div className={styles.page}>
      <Card className={styles.titleRow}>
        <div className={styles.title}>{t('glossary.title')}</div>
        {GLossaryRole && GLossaryRole.includes(Operation.ADD_GLOSSARY) && (
          <Button size="large" type="primary" className={styles.createButton} onClick={handleClickCreate}>
            {t('common.button.create')}
          </Button>
        )}
      </Card>

      {glossaryDisplayType === GlossaryDisplayType.RELATION && (
        <>
          <div className={styles.autosuggestInputContainer}>
            <DisplayTypeSwitch currentType={glossaryDisplayType} onChange={changeDisplayType} />
            <span style={{ marginLeft: '20px' }} />
            <AutosuggestInput setCurrentId={setCurrentId} />
          </div>
          <Card className={styles.glossaryTreeContainer}>
            <GlossaryTree ref={childrenRef} rootNode={selector.glossaryData} />
          </Card>
        </>
      )}

      {glossaryDisplayType === GlossaryDisplayType.LIST && (
        <Card className={styles.glossaryListContainer}>
          <GlossaryListView
            ref={childrenRef}
            glossaryDisplayType={glossaryDisplayType}
            changeDisplayType={changeDisplayType}
          />
        </Card>
      )}
    </div>
  );
}
