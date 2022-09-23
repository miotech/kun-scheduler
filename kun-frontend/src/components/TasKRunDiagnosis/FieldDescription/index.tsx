import React, { useState } from 'react';
import { Collapse } from 'antd';
import { CaretDownOutlined, CaretRightOutlined } from '@ant-design/icons';

import useI18n from '@/hooks/useI18n';

import styles from './index.less';

interface Props {
  collapsible?: boolean;
}

const { Panel } = Collapse;

const filedsI18Keys = [
  'taskRun.diagnosis.fieldDescription.memoryUsage',
  'taskRun.diagnosis.fieldDescription.memoryUsagePercent',
  'taskRun.diagnosis.fieldDescription.executor.num.peak',
  'taskRun.diagnosis.fieldDescription.executor.num.median',
  'taskRun.diagnosis.fieldDescription.executor.memory.peak',
  'taskRun.diagnosis.fieldDescription.executor.memory.median',
  'taskRun.diagnosis.fieldDescription.driver.memory.peak',
  'taskRun.diagnosis.fieldDescription.driver.memory.median',
];

const FiledDescription: React.FC<Props> = ({ collapsible }) => {
  const t = useI18n();
  const [activeKey, setActiveKey] = useState('1');

  const toggleActiveKey = () => {
    if (collapsible) {
      setActiveKey(activeKey ? '' : '1');
    }
  };

  return (
    <div className={styles.Container}>
      <Collapse
        activeKey={activeKey}
        collapsible={collapsible ? 'header' : 'disabled'}
        ghost
        expandIconPosition="right"
        expandIcon={({ isActive }) => (
          <div onClick={toggleActiveKey}>{isActive ? <CaretDownOutlined /> : <CaretRightOutlined />}</div>
        )}
      >
        <Panel
          className="fileds-description"
          header={<div onClick={toggleActiveKey}>{t('taskRun.diagnosis.fieldDescription.title')}</div>}
          key={1}
        >
          {filedsI18Keys.map(i18Key => (
            <div key={i18Key}>{t(i18Key)}</div>
          ))}
        </Panel>
      </Collapse>
    </div>
  );
};

export default FiledDescription;
