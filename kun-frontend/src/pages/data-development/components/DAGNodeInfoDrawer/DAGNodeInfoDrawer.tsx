import React, { memo } from 'react';
import { Link } from 'umi';
import { Button, Drawer, Row, Descriptions } from 'antd';
import SafeUrlAssembler from 'safe-url-assembler';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

interface OwnProps {
  visible?: boolean;
  currentTaskDef?: TaskDefinition | null;
  getContainer?: string | HTMLElement | (() => HTMLElement) | false;
}

type Props = OwnProps;

export const DAGNodeInfoDrawer: React.FC<Props> = memo(function DAGNodeInfoDrawer(props) {

  const {
    visible = false,
    currentTaskDef,
    getContainer,
  } = props;

  return (
    <Drawer
      visible={visible}
      title={currentTaskDef?.name || ''}
      placement="right"
      closable={false}
      width={325}
      // zIndex={10}
      mask={false}
      getContainer={getContainer || false}
      style={{ position: 'absolute', top: '48px', height: 'calc(100% - 48px)' }}
    >
      <Descriptions column={1}>
        <Descriptions.Item label="Task name">
          {currentTaskDef?.name}
        </Descriptions.Item>
        <Descriptions.Item label="Task Type">
          {currentTaskDef?.taskTemplateName}
        </Descriptions.Item>
        <Descriptions.Item label="Owner">
          {currentTaskDef?.owner}
        </Descriptions.Item>
        <Descriptions.Item label="Create time">
          {currentTaskDef?.createTime}
        </Descriptions.Item>
        <Descriptions.Item label="Last update time">
          {currentTaskDef?.lastUpdateTime}
        </Descriptions.Item>
      </Descriptions>
      <Row>
        <Link
          to={SafeUrlAssembler()
            .template('/data-development/task-definition/:taskDefId').param({
              taskDefId: currentTaskDef?.id,
            }).toString()}
        >
          <Button type="primary">
            Go to definition
          </Button>
        </Link>
      </Row>
    </Drawer>
  );
});
