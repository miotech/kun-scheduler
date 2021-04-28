import React, { useMemo } from 'react';
import { DatabaseOutlined, TagsOutlined } from '@ant-design/icons';
import { Link } from 'umi';
import useI18n from '@/hooks/useI18n';
import styles from './index.less';

function Homepage() {
  const t = useI18n();

  const functionsList = useMemo(
    () => [
      {
        key: 'dataDiscovery',
        title: t('common.pageTitle.datasets'),
        path: '/data-discovery/dataset',
        icon: <DatabaseOutlined className={styles.functionItemIcon} style={{ marginBottom: 20 }} />,
      },
      {
        key: 'glossary',
        title: t('common.pageTitle.glossary'),
        path: '/data-discovery/glossary',
        icon: <TagsOutlined className={styles.functionItemIcon} style={{ marginBottom: 20 }} />,
      },
    ],
    [t],
  );

  return (
    <div className={styles.page}>
      <div className={styles.functionsArea}>
        {functionsList.map(functionItem => (
          <Link to={functionItem.path} key={functionItem.key} className={styles.functionItem}>
            {functionItem.icon}
            <span className={styles.functionItemTitle}>{functionItem.title}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}

export default Homepage;
