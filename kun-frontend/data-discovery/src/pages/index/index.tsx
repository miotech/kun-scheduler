import React, { useMemo } from 'react';
import { DatabaseOutlined } from '@ant-design/icons';
import { Link } from 'umi';
import Card from '@/components/Card/Card';
import useI18n from '@/hooks/useI18n';
import styles from './index.less';

function Homepage() {
  const t = useI18n();

  const functionsList = useMemo(
    () => [
      {
        key: 'dataDiscovery',
        title: t('common.pageTitle.datasets'),
        path: '/data-discovery',
        icon: (
          <DatabaseOutlined
            className={styles.functionItemIcon}
            style={{ marginBottom: 20 }}
          />
        ),
      },
    ],
    [t],
  );

  return (
    <div className={styles.page}>
      <div className={styles.functionsArea}>
        {functionsList.map(functionItem => (
          <Link
            to={functionItem.path}
            key={functionItem.key}
            className={styles.functionItem}
          >
            {functionItem.icon}
            <span className={styles.functionItemTitle}>
              {functionItem.title}
            </span>
          </Link>
        ))}
      </div>

      <Card className={styles.recentHistoryArea}>
        <div className={styles.recentHistoryAreaTitle}>
          {t('homepage.recentHistory')}
        </div>

        {/* <div className={styles.historyTable}>
          <div>134</div>
        </div> */}
      </Card>
    </div>
  );
}

export default Homepage;
