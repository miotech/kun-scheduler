import React, { useCallback, useRef, useMemo } from 'react';
import { Button } from 'antd';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import Card from '@/components/Card/Card';
import { useRequest } from 'ahooks';

import { queryGlossaryRole } from '@/services/glossary';
import { Operation } from '@/definitions/Glossary.type';
import AutosuggestInput from './components/AutosuggestInput/AutosuggestInput';
import GlossaryTree from './components/GlossaryTree/GlossaryTree';

import styles from './index.less';

export default function Glossary() {
  const t = useI18n();
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
      <div className={styles.autosuggestInputContainer}>
        <AutosuggestInput setCurrentId={setCurrentId} />
      </div>

      <Card className={styles.glossaryTreeContainer}>
        <GlossaryTree ref={childrenRef} rootNode={selector.glossaryData} />
      </Card>
    </div>
  );
}
