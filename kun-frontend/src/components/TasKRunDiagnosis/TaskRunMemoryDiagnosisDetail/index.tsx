import React, { memo } from 'react';

import useI18n from '@/hooks/useI18n';
import { TaskRunDiagnosis } from '@/definitions/TaskRun.type';

import styles from './index.less';

interface Props {
  taskRunDiagnosis: TaskRunDiagnosis;
}

const generateFiled = (dataIndex: keyof TaskRunDiagnosis, unit: string, i18Key: string) => {
  return {
    dataIndex,
    unit,
    i18Key,
  };
};

const fileds = [
  generateFiled('memorySeconds', 'MB', 'taskRun.diagnosis.memoryUsage'),
  generateFiled('memoryUsagePercentage', '%', 'taskRun.diagnosis.memoryUsagePercent'),
  generateFiled('maxExecutorNumber', '', 'taskRun.diagnosis.executor.num.peak'),
  generateFiled('medianExecutorNumber', '', 'taskRun.diagnosis.executor.num.median'),
  generateFiled('maxExecutorMemory', 'MB', 'taskRun.diagnosis.executor.memory.peak'),
  generateFiled('medianExecutorMemory', 'MB', 'taskRun.diagnosis.executor.memory.median'),
  generateFiled('maxDriverMemory', 'MB', 'taskRun.diagnosis.driver.memory.peak'),
  generateFiled('medianDriverMemory', 'MB', 'taskRun.diagnosis.driver.memory.median'),
];

const fomartFieldValue = (value: string | number | undefined, unit: string) => {
  return `${value || 0}${unit}`;
};

const TaskRunMemoryDiagnosisDetail: React.FC<Props> = memo(({ taskRunDiagnosis }) => {
  const t = useI18n();

  return (
    <div className={styles.Container}>
      {fileds.map(field => {
        return (
          <div className={styles.DianosisItem}>
            <div className={styles.Label}>{t(field.i18Key)}:</div>
            <div className={styles.Value}>{fomartFieldValue(taskRunDiagnosis?.[field.dataIndex], field.unit)}</div>
          </div>
        );
      })}
    </div>
  );
});

export default TaskRunMemoryDiagnosisDetail;
