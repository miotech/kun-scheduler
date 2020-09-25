import React from 'react';
import { RatioCard } from './RatioCard';
import { Meta } from '@storybook/react';
import { Col, Row } from 'antd';

import 'antd/dist/antd.min.css';

export default {
  title: 'components/Monitoring/RatioCard',
  component: RatioCard,
} as Meta;

export const RatioCardDemo = () => {
  return (
    <Row justify="space-around">
      <Col span={8}>
        <RatioCard
          title="Covered Ratio"
          numerator={523}
          denominator={612}
        />
      </Col>
      <Col span={8}>
        <RatioCard
          title="Count of Long-existing Failed Case"
          numerator={54}
          denominator={612}
          titleTooltip="This is a tooltip"
        />
      </Col>
      <Col span={8}>
        <RatioCard
          title="Covered Ratio"
          numerator={420}
          denominator={612}
        />
      </Col>
    </Row>
  );
};
