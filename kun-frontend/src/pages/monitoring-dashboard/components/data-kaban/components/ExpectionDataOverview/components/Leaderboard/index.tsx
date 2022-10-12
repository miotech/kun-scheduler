import React, { ReactNode, useMemo } from 'react';
import { Popover, Button } from 'antd';
import { InfoCircleOutlined, SwapRightOutlined } from '@ant-design/icons';
import { Progress } from '@/components/ui-components';

import styles from './index.less';

type DataItem = {
  label: string;
  percent: string | number;
  value: string | number;
};

interface Props {
  title: string | ReactNode;
  popoverContent?: string | ReactNode;
  progressColor?: string;
  data: DataItem[];
}

const Leaderboard: React.FC<Props> = ({ data, progressColor, title, popoverContent }) => {
  const sortedData = useMemo(() => {
    return data.sort((pre, cur) => {
      return Number(pre.value) > Number(cur.value) ? -1 : 1;
    });
  }, [data]);
  return (
    <div className={styles.Container}>
      <div className={styles.Header}>
        {title}
        <Popover content={popoverContent}>
          <InfoCircleOutlined size={14} style={{ marginLeft: '9px', cursor: 'pointer' }} />
        </Popover>
      </div>
      {sortedData.map(item => {
        return (
          <Progress
            strokeLinecap="square"
            key={item.label}
            label={item.label}
            percent={Number(item.percent)}
            showInfo
            strokeColor={progressColor}
          />
        );
      })}
      <div className={styles.Footer}>
        <Button type="link">
          查看所有
          <SwapRightOutlined color="#49A9DE" />
        </Button>
      </div>
    </div>
  );
};

export { Leaderboard };
