import React from 'react';
import { Row, Col } from 'antd';
import { Card, Statistic } from '@/components/ui-components';

import { Leaderboard } from './components';

import styles from './index.less';

interface Props {}

const { CardGrid } = Card;

const data = [
  { value: 90, percent: 90, label: 'test1' },
  { value: 50, percent: 50, label: 'test2' },
  { value: 70, percent: 70, label: 'test3' },
  { value: 85, percent: 85, label: 'test4' },
  { value: 30, percent: 30, label: 'test5' },
];

const ExpectionDataOverview: React.FC<Props> = () => {
  return (
    <Card title="异常数据概览">
      <CardGrid span={24}>
        <div className={styles.Statistic}>
          <Row gutter={16}>
            <Col span={8}>
              <Statistic value={126777} color="#FFBE3D" footer="执行失败测试用例总数" />
            </Col>
            <Col span={8}>
              <Statistic value={123333} color="#FF6336" footer="长期失败测试用例数" />
            </Col>
            <Col span={8}>
              <Statistic value={122222} color="#3EC3CB" footer="执行中测试用例总数" />
            </Col>
          </Row>
        </div>
        <Card
          title={<div style={{ fontSize: '16px', color: '#666666' }}>异常数据测试</div>}
          split
          gutter={64}
          headStyle={{ padding: '0' }}
        >
          <CardGrid span={12}>
            <Leaderboard title="所属人分布" progressColor="#FFBE3D" data={data} />
          </CardGrid>
          <CardGrid span={12}>
            <Leaderboard title="DW Layer分布" progressColor="#FFBE3D" data={data} />
          </CardGrid>
        </Card>
      </CardGrid>
    </Card>
  );
};

export { ExpectionDataOverview };
