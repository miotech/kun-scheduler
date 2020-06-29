import React from 'react';
import { Row } from 'antd';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { FormInstance } from 'antd/es/form';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import { TaskDefinitionParamItem } from '@/components/TaskDefinitionParamItem';

import styles from './BodyForm.less';

interface SchedulingConfigProps {
  initTaskDefinition?: TaskDefinition;
  taskTemplate?: TaskTemplate;
  form: FormInstance;
}

export const ParamConfig: React.FC<SchedulingConfigProps> = props => {
  // const t = useI18n();
  const { initTaskDefinition, taskTemplate } = props;

  // maps each display parameter to form item
  const paramItems = (taskTemplate?.displayParameters || []).map(displayParam => {
    return (
      <Row key={`form-item-key-${displayParam.name}-${displayParam.type}`}>
        <TaskDefinitionParamItem
          parameter={displayParam}
          wrapFormItem={{
            name: ['taskPayload', 'taskConfig', displayParam.name],
            initialValue: initTaskDefinition?.taskPayload?.taskConfig?.[displayParam.name] || undefined,
          }}
        />
      </Row>
    );
  });

  return (
    <div className={styles.ParamConfig}>
      {paramItems}
    </div>
  );
};
