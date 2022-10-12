import React from 'react';
import { Space } from 'antd';
import { Card, Statistic } from '@/components/ui-components';

interface Props {}

const { CardGrid } = Card;

const TaskDataOverview: React.FC<Props> = () => {
  return (
    <Card title="任务数据概览" style={{ height: '100%' }}>
      <CardGrid span={24}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Statistic value={12333} color="#0F4C75" footer="延时且未执行任务数" />
          <Statistic value={12333} color="#49A9DE" footer="正在执行任务数" />
        </Space>
      </CardGrid>
    </Card>
  );
};

export { TaskDataOverview };
