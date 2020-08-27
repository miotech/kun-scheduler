import React, { memo } from 'react';
import { KunLoading } from '@/components/KunLoading';
import useI18n from '@/hooks/useI18n';

const LoadingLayout = memo(() => {
  const t = useI18n();

  return (
    <div id="layout-loading" className="layout-loading">
      <div className="layout-loading__inner-wrapper">
        <div className="layout-loading__svg">
          <KunLoading />
        </div>
        <div className="layout-loading__text">{t('common.loading')}</div>
      </div>
    </div>
  );
});

export default LoadingLayout;
