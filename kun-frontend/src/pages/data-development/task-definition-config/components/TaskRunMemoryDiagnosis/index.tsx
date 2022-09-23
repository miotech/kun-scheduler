import React, { useCallback, useState } from 'react';
import { Button } from 'antd';
import { useRequest } from 'ahooks';

import useI18n from '@/hooks/useI18n';
import TaskRunMemoryDiagnosisDetail from '@/components/TasKRunDiagnosis/TaskRunMemoryDiagnosisDetail';
import { fetchLatestTaskDiagnosis } from '@/services/taskDiagnosis';
import { TaskRunDiagnosis } from '@/definitions/TaskRun.type';

import { LatestMemoryDiagnosisModal, FieldDescriptionPopover } from './components';
import styles from './index.less';

interface Props {
  taskDefId: string;
}

const TaskRunMemoryDiagnosis: React.FC<Props> = ({ taskDefId }) => {
  const [modalVisible, setModalVisible] = useState<boolean>(false);

  const t = useI18n();
  const { data } = useRequest(fetchLatestTaskDiagnosis.bind(null, taskDefId, { limit: 1 }), {
    refreshDeps: [taskDefId],
  });

  const handleModalClose = useCallback(() => {
    setModalVisible(false);
  }, []);
  return (
    <div className={styles.Container}>
      <div className={styles.Header}>
        <div className={styles.Title}>{t('taskRun.diagnosis.title')}</div>
        <FieldDescriptionPopover />
      </div>
      <div className={styles.Content}>
        <TaskRunMemoryDiagnosisDetail taskRunDiagnosis={data?.[0] as TaskRunDiagnosis} />
      </div>
      <div className={styles.Footer}>
        <Button
          type="link"
          onClick={() => {
            setModalVisible(true);
          }}
        >
          {t('taskRun.diagnosis.action.lookUp')}
        </Button>
      </div>
      <LatestMemoryDiagnosisModal visible={modalVisible} onClose={handleModalClose} taskDefId={taskDefId} />
    </div>
  );
};

export default TaskRunMemoryDiagnosis;
