import React from 'react';
import { Card } from '@/components/ui-components';

interface Props {}

const { CardGrid } = Card;

const DWLayerTestCaseOverview: React.FC<Props> = () => {
  return (
    <div>
      <Card title="DW Layer测试用例概览">
        <CardGrid>
          <Card title="测试用例覆盖率" />
        </CardGrid>
        <CardGrid>
          <Card title="5大数据指标统计" />
        </CardGrid>
      </Card>
    </div>
  );
};

export { DWLayerTestCaseOverview };
