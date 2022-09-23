import React, { useEffect } from 'react';
import { Modal } from 'antd';

import { fetchLatestTaskDiagnosis } from '@/services/taskDiagnosis';
import { useRequest } from 'ahooks';
import { KunSpin } from '@/components/KunSpin';

import FieldDescriptionPopover from '../FieldsDescriptionPopover';
import ExecutorMemoryUsageLineChart from '../ExecutorMemoryUsageLineChart';
import MemoryUsageLineChart from '../MemoryUsageLineChart';

import styles from './index.less';

interface Props {
  visible: boolean;
  onClose: () => void;
  taskDefId: string;
}

const LatestMemoryDiagnosisModal: React.FC<Props> = ({ visible, onClose, taskDefId }) => {
  const { run, data, loading } = useRequest(fetchLatestTaskDiagnosis, {
    manual: true,
  });

  useEffect(() => {
    if (visible && taskDefId) {
      run(taskDefId);
    }
  }, [visible, taskDefId, run]);

  return (
    <Modal
      className={styles.ModalContainer}
      visible={visible}
      onCancel={onClose}
      title={<FieldDescriptionPopover />}
      footer={null}
      width={1000}
    >
      <KunSpin spinning={loading}>
        <div className={styles.ChartContainer}>
          <MemoryUsageLineChart data={data ?? []} />
        </div>
        <div className={styles.ChartContainer}>
          <ExecutorMemoryUsageLineChart data={data ?? []} />
        </div>
      </KunSpin>
    </Modal>
  );
};

export default LatestMemoryDiagnosisModal;
