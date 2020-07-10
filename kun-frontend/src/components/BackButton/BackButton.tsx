import React, { memo } from 'react';
import { Link } from 'umi';
import useI18n from '@/hooks/useI18n';
import useBackUrlFunc from '@/hooks/useBackUrlFunc';

import styles from './BackButton.less';

interface Props {
  defaultUrl: string;
}

export default memo(function BackButton({ defaultUrl }: Props) {
  const t = useI18n();

  const { getBackUrl } = useBackUrlFunc();

  return (
    <div className={styles.backButtonRow}>
      <Link to={getBackUrl(defaultUrl)}>
        {'< '}
        {t('common.back')}
      </Link>
    </div>
  );
});
