import React, { memo } from 'react';
import { CodeOutlined, ConsoleSqlOutlined, SettingOutlined } from '@ant-design/icons';

interface OwnProps {
  name?: string;
}

type Props = OwnProps;

export const TaskTemplateIcon: React.FC<Props> = memo(function TaskTemplateIcon({ name }) {
  if (!name) {
    return <SettingOutlined />;
  }
  // sql
  if (name.match(/sql/i)) {
    return <ConsoleSqlOutlined />;
  }
  // spark
  if (name.match(/bash/i)) {
    return <CodeOutlined />;
  }
  return <SettingOutlined />;
});
