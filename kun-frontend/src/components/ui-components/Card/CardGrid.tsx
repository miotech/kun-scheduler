import React from 'react';
import Cls from 'classnames';
import { ColProps } from 'antd/es/col';

import './CardGrid.less';

interface Props extends ColProps {}

const CardGrid: React.FC<Props> = ({ children, className }) => {
  return <div className={Cls('miotech-card-grid', className)}>{children}</div>;
};

CardGrid.displayName = 'CardGrid';

export { CardGrid };
