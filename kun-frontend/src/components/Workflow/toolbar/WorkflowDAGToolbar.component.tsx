import React, { memo, useCallback } from 'react';
import { Tool } from 'react-svg-pan-zoom';
import { Button, Radio, Space } from 'antd';
import Icon, { ZoomInOutlined, ZoomOutOutlined } from '@ant-design/icons';

import { ReactComponent as CursorDefaultIcon } from '@/assets/icons/cursor-default-outline.svg';
import { ReactComponent as BoxSelectionIcon } from '@/assets/icons/box-selection.svg';
import { ReactComponent as ResetViewIcon } from '@/assets/icons/reset-view.svg';

import './WorkflowDAGToolbar.global.less';

// eslint-disable-next-line @typescript-eslint/naming-convention
export type TOOL_BOX_SELECT = 'box-select';

interface OwnProps {
  currentTool: Tool | TOOL_BOX_SELECT;
  onChangeTool: (nextTool: Tool | TOOL_BOX_SELECT) => void;
  onClickReset: () => any;
}

type Props = OwnProps;

export const WorkflowDAGToolbar: React.FC<Props> = memo(function WorkflowDAGToolbar(props) {
  const {
    currentTool,
    onChangeTool,
    onClickReset,
  } = props;

  const handleToolChange = useCallback((ev) => {
    if (onChangeTool) {
      onChangeTool(ev.target.value);
    }
  }, [
    onChangeTool,
  ]);

  return (
    <div className="workflow-dag-toolbar" data-tid="workflow-dag-toolbar">
      <Space>
        <Radio.Group value={currentTool} onChange={handleToolChange}>
          {/* Auto mode */}
          <Radio.Button value="auto" >
            <Icon component={CursorDefaultIcon} />
          </Radio.Button>
          {/* box selection mode */}
          <Radio.Button value="box-select">
            <Icon component={BoxSelectionIcon} />
          </Radio.Button>
          {/* zoom-in mode */}
          <Radio.Button value="zoom-in">
            <ZoomInOutlined />
          </Radio.Button>
          {/* zoom-out mode */}
          <Radio.Button value="zoom-out">
            <ZoomOutOutlined />
          </Radio.Button>
        </Radio.Group>
        <Button
          onClick={onClickReset}
          icon={<Icon component={ResetViewIcon} />}
        />
      </Space>
    </div>
  );
});
