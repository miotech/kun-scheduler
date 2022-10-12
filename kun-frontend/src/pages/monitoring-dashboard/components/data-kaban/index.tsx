import React from 'react';
import { Row, Col } from 'antd';

import { ExpectionDataOverview, TaskDataOverview, DWLayerTestCaseOverview } from './components';

import styles from './index.less';

interface Props {}

const DataKanban: React.FC<Props> = () => {
  return (
    <div className={styles.Container} style={{ width: '100%' }}>
      <Row gutter={16}>
        <Col span={18}>
          <ExpectionDataOverview />
        </Col>
        <Col span={6}>
          <TaskDataOverview />
        </Col>
      </Row>
      <Row>
        <Col>
          <DWLayerTestCaseOverview />
        </Col>
      </Row>
    </div>
  );
};

export { DataKanban };
