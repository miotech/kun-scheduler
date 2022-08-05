import React from 'react';
import { Modal } from 'antd';
import { LineageDataset } from '@/definitions/ReferenceData.type';
import useI18n from '@/hooks/useI18n';
import styles from './TimeCited.less';

interface Props {
  timeCitedVisible: boolean;
  setTimeCitedVisible: (value: boolean) => void;
  currentTableName: string | undefined;
  timeCited: LineageDataset[] | undefined;
}
const TimeCited: React.FC<Props> = (props: Props) => {
  const t = useI18n();
  const i18n = 'dataDiscovery.referenceData.table';
  const { timeCitedVisible, setTimeCitedVisible, currentTableName, timeCited } = props;
  const handleCancel = () => {
    setTimeCitedVisible(false);
  };

  return (
    <Modal
      title={t(`${i18n}.modal.refTableTitle`)}
      visible={timeCitedVisible}
      onCancel={handleCancel}
      onOk={() => setTimeCitedVisible(false)}
    >
      <div className={styles.content}>
        <div className={styles.title}>{t(`${i18n}.tableName`)}</div>
        <div className={styles.con}>{currentTableName}</div>

        <div className={styles.title}>{t(`${i18n}.modal.refTableNumber`)}</div>
        <div className={styles.con}>{timeCited?.length}</div>

        <div className={styles.title}>{t(`${i18n}.modal.refTableName`)}</div>
        <div style={{ height: '300px', overflow: 'scroll', marginTop: '10px' }}>
          {timeCited?.map((item: LineageDataset) => (
            <div className={styles.con}>{item[Object.keys(item)[0]]}</div>
          ))}
        </div>
      </div>
    </Modal>
  );
};

export default TimeCited;
