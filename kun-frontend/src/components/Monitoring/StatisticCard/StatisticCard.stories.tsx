import React from 'react';
import { Meta } from '@storybook/react';
import { StatisticCard } from './StatisticCard';
import { Row, Col } from 'antd';

import 'antd/dist/antd.min.css';

export default {
  title: 'components/Monitoring/StatisticCard',
  component: StatisticCard,
} as Meta;

export const StatisticCardDemo = () => {
  return (
    <Row gutter={[16, 0]}>
      <Col span={6}>
        <StatisticCard
          title="Success (Last 24 hours)"
          value={1455}
          textTheme="success"
        />
      </Col>
      <Col span={6}>
        <StatisticCard
          title="Failed (Last 24 hours)"
          value={23}
          textTheme="failed"
        />
      </Col>
      <Col span={6}>
        <StatisticCard
          title="Running"
          value={2523}
          textTheme="running"
        />
      </Col>
      <Col span={6}>
        <StatisticCard
          title="Total Task Count"
          value={15313}
        />
      </Col>
    </Row>
  );
};
