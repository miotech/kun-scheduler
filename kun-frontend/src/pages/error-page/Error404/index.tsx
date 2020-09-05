import React, { memo } from 'react';
import { useHistory } from 'umi';

import img404 from '@/assets/images/404.png';

import useI18n from '@/hooks/useI18n';

import ErrorPageContainer from '../components/ErrorPageContainer/ErrorPageContainer';

import styles from './index.less';

export default memo(function Error401() {
  const t = useI18n();

  const history = useHistory();

  return (
    <ErrorPageContainer>
      <div className={styles.error404}>
        <img className={styles.errorImg} src={img404} alt="404" />
        <span className={styles.errorMessage}>{t('common.errorPage.404')}</span>
        <span
          className={styles.backButton}
          onClick={() => {
            history.goBack();
          }}
        >
          {t('common.back')}
        </span>
      </div>
    </ErrorPageContainer>
  );
});
