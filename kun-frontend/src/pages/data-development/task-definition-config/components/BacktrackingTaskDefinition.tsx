import React from 'react';
import { Row, Col } from 'antd';
import useI18n from '@/hooks/useI18n';
import { useRequest } from 'ahooks';
import { fetchDefinitionBackTracking } from '@/services/data-development/task-definitions';
import { useParams } from 'umi';
import styles from './BacktrackingTaskDefinition.less';

export const BacktrackingTaskDefinition = () => {
  const t = useI18n();
  const params = useParams<{ taskDefId: string }>();
  const { data } = useRequest(() => fetchDefinitionBackTracking(params.taskDefId));
  return (
    <div className={styles.content}>
      <Row>
        <Col span={6}>
          <div className={styles.label}>
            {t('dataDevelopment.definition.backtrackingTaskDefinition.avgTaskRunTimeLastSevenTimes')}
          </div>
          <div className={styles.label}>
            {t('dataDevelopment.definition.backtrackingTaskDefinition.backtrackingTaskDefinitionPriority')}
          </div>
          <div className={styles.label}>
            {t('dataDevelopment.definition.backtrackingTaskDefinition.backtrackingTaskDefinitionName')}
          </div>
          <div className={styles.label}>
            {t('dataDevelopment.definition.backtrackingTaskDefinition.backtrackingTaskDefinitionDeadline')}
          </div>
        </Col>
        <Col span={18}>
          <div className={styles.value}>
            {data?.avgTaskRunTimeLastSevenTimes ? `${data?.avgTaskRunTimeLastSevenTimes}min` : ''}
          </div>
          <div className={styles.value}>{data?.backtrackingTaskDefinition?.priority}</div>
          <div className={styles.value}>{data?.backtrackingTaskDefinition?.definitionName}</div>
          <div className={styles.value}>{data?.backtrackingTaskDefinition?.deadline}</div>
        </Col>
      </Row>
    </div>
  );
};
