import React, { memo } from 'react';
import { useHistory } from 'umi';

import img401 from '@/assets/images/401.png';

import useI18n from '@/hooks/useI18n';

import ErrorPageContainer from '../components/ErrorPageContainer/ErrorPageContainer';

import styles from './index.less';

export default memo(function Error401() {
  const t = useI18n();

  const history = useHistory();

  return (
    <ErrorPageContainer>
      <div className={styles.error401}>
        <div className={styles.errorImgContainer}>
          <img className={styles.errorImg} src={img401} alt="401" />
        </div>
        <span className={styles.errorMessage}>{t('common.errorPage.401')}</span>
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
