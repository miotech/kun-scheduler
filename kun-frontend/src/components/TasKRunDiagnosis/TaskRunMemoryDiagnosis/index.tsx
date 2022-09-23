import React, { useState, useEffect } from 'react';
import { useRequest } from 'ahooks';
import { Modal, Button } from 'antd';
import { KunSpin } from '@/components/KunSpin';

import { fetchTaskDiagnosisDetail } from '@/services/taskDiagnosis';
import { TaskRunDiagnosis } from '@/definitions/TaskRun.type';
import useI18n from '@/hooks/useI18n';
import FieldDescription from '../FieldDescription';
import TaskRunMemoryDiagnosisDetail from '../TaskRunMemoryDiagnosisDetail';

import styles from './index.less';

interface Props {
  taskRunId: string;
  shouldModal?: boolean;
}

const generateDiagnosisDetailStyle = (shouldModal?: boolean): React.CSSProperties => {
  return shouldModal
    ? {
        paddingLeft: '80px',
      }
    : {};
};

const renderTaskRunMemoryDiagnosis = (diagnosisData: TaskRunDiagnosis, loading: boolean, shouldModal?: boolean) => {
  return (
    <KunSpin spinning={loading}>
      <div className={styles.Container}>
        <div className={styles.DiagnosisDetail} style={generateDiagnosisDetailStyle(shouldModal)}>
          <TaskRunMemoryDiagnosisDetail taskRunDiagnosis={diagnosisData} />
        </div>
        <FieldDescription collapsible />
      </div>
    </KunSpin>
  );
};

const TaskRunMemoryDiagnosis: React.FC<Props> = ({ taskRunId, shouldModal }) => {
  const t = useI18n();
  const [visible, setVisible] = useState(false);
  const { data, loading, run } = useRequest(fetchTaskDiagnosisDetail.bind(null, taskRunId), {
    manual: true,
  });

  useEffect(() => {
    if (taskRunId) {
      if (!shouldModal) {
        run();
      }

      if (shouldModal && visible) {
        run();
      }
    }
  }, [visible, shouldModal, taskRunId, run]);

  if (shouldModal) {
    return (
      <React.Fragment>
        <Button
          type="link"
          onClick={() => {
            setVisible(true);
          }}
        >
          {t('common.button.view')}
        </Button>
        <Modal
          destroyOnClose
          visible={shouldModal && visible}
          footer={null}
          onCancel={() => {
            setVisible(false);
          }}
          title={t('common.taskRun.memory.diagnosis')}
          width={800}
        >
          <React.Fragment>
            {renderTaskRunMemoryDiagnosis(data as TaskRunDiagnosis, loading, shouldModal)}
          </React.Fragment>
        </Modal>
      </React.Fragment>
    );
  }

  return <React.Fragment>{renderTaskRunMemoryDiagnosis(data as TaskRunDiagnosis, loading)}</React.Fragment>;
};

export { TaskRunMemoryDiagnosis };
