import React, { memo } from 'react';
import { dayjs } from '@/utils/datetime-utils';
import SafeUrlAssembler from 'safe-url-assembler';

import { Button, Drawer, Row, Descriptions } from 'antd';
import { Link } from 'umi';
import { UsernameText } from '@/components/UsernameText';
import useI18n from '@/hooks/useI18n';

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

  const t = useI18n();

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
        <Descriptions.Item label={t('dataDevelopment.definition.property.name')}>
          {currentTaskDef?.name}
        </Descriptions.Item>
        <Descriptions.Item label={t('dataDevelopment.definition.property.taskTemplateName')}>
          {currentTaskDef?.taskTemplateName}
        </Descriptions.Item>
        <Descriptions.Item label={t('dataDevelopment.definition.property.owner')}>
          {currentTaskDef?.owner ? <UsernameText userId={currentTaskDef.owner} /> : ''}
        </Descriptions.Item>
        <Descriptions.Item label={t('dataDevelopment.definition.property.createTime')}>
          {currentTaskDef?.createTime ? dayjs(currentTaskDef.createTime).format('YYYY-MM-DD HH:mm') : '-'}
        </Descriptions.Item>
        <Descriptions.Item label={t('dataDevelopment.definition.property.lastUpdateTime')}>
          {currentTaskDef?.lastUpdateTime ? dayjs(currentTaskDef.lastUpdateTime).format('YYYY-MM-DD HH:mm') : '-'}
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
            {t('dataDevelopment.goToDefinitionDetails')}
          </Button>
        </Link>
      </Row>
    </Drawer>
  );
});
