import React, { ReactNode } from 'react';
import { Statistic as AntStatistic, StatisticProps } from 'antd';
import { convertHex2Rgba } from '@/utils/format';

import styles from './index.less';

interface Props extends StatisticProps {
  color: string;
  footer?: string | ReactNode;
}

const generateBackgroundStyle = (color: string): React.CSSProperties => {
  const hex = color.replace('#', '');
  return {
    background: `linear-gradient(180deg, ${convertHex2Rgba(hex, 0.08)} -43.64%, ${convertHex2Rgba(hex, 0)} 121.82%)`,
  };
};

const Statistic: React.FC<Props> = ({ color, valueStyle, footer, ...resetProps }) => {
  return (
    <div className={styles.Container} style={generateBackgroundStyle(color)}>
      <AntStatistic {...resetProps} valueStyle={{ ...valueStyle, color, backgroundColor: 'transparent' }} />
      <div className={styles.Footer}>{footer}</div>
    </div>
  );
};

export { Statistic };
