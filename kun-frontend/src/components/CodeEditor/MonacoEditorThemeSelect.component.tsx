import React, { memo } from 'react';
import { Select } from 'antd';

interface OwnProps {
  value?: string;
  onChange?: (nextTheme: string) => any;
  className?: string;
}

type Props = OwnProps;

export const MonacoEditorThemeSelect: React.FC<Props> = memo(function MonacoEditorThemeSelect(props) {
  const { value, onChange, className } = props;
  return (
    <Select
      value={value}
      onChange={onChange}
      className={className}
    >
      <Select.Option value="light">Light</Select.Option>
      <Select.Option value="xcode">Xcode</Select.Option>
      <Select.Option value="github">GitHub</Select.Option>
      <Select.Option value="vs-dark">VS Dark</Select.Option>
      <Select.Option value="monokai">Monokai</Select.Option>
      <Select.Option value="blackboard">BlackBoard</Select.Option>
    </Select>
  );
});
