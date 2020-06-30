import React, { useEffect, useCallback } from 'react';
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

  const { selector, dispatch } = useRedux(state => state.glossary);

  useEffect(() => {
    if (!selector.glossaryData) {
      dispatch.glossary.fetchRootNodeChildGlossary();
    }
  }, [dispatch.glossary, selector.glossaryData]);

  const handleClickCreate = useCallback(() => {
    history.push('/data-discovery/glossary/create');
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
        <GlossaryTree rootNode={selector.glossaryData} />
      </Card>
    </div>
  );
}
