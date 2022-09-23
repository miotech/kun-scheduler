import React from 'react';
import { Popover } from 'antd';
import { QuestionCircleFilled } from '@ant-design/icons';

import FieldDescription from '@/components/TasKRunDiagnosis/FieldDescription';

const FieldDescriptionPopover: React.FC = () => {
  return (
    <Popover trigger="click" placement="top" content={<FieldDescription />}>
      <QuestionCircleFilled size={12} style={{ color: '#ff4d4f' }} />
    </Popover>
  );
};

export default FieldDescriptionPopover;
