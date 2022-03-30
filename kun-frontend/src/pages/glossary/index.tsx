import React, { useEffect, useCallback, useRef,useMemo } from 'react';
import { history } from 'umi';
import { Button } from 'antd';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import Card from '@/components/Card/Card';

import AutosuggestInput from './components/AutosuggestInput/AutosuggestInput';
import GlossaryTree from './components/GlossaryTree/GlossaryTree';

import styles from './index.less';

export default function Glossary() {
  const t = useI18n();
  const childrenRef = useRef(null);
  const { selector, dispatch } = useRedux(state => state.glossary);


  const handleClickCreate = useCallback(() => {
    childrenRef.current.create();
  }, []);

  return (
    <div className={styles.page}>
      <Card className={styles.titleRow}>
        <div className={styles.title}>{t('glossary.title')}</div>
        <Button
          size="large"
          type="primary"
          className={styles.createButton}
          onClick={handleClickCreate}
        >
          {t('common.button.create')}
        </Button>
      </Card>
      <div className={styles.autosuggestInputContainer}>
        <AutosuggestInput />
      </div>

      <Card className={styles.glossaryTreeContainer}>
        <GlossaryTree ref={childrenRef} rootNode={selector.glossaryData} />
      </Card>
    </div>
  );
}
