import React from 'react';
import { Row } from 'antd';

import { DisplayParameter, TaskTemplate } from '@/definitions/TaskTemplate.type';
import { FormInstance } from 'antd/es/form';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import { TaskDefinitionParamItem } from '@/components/TaskDefinitionParamItem';

import TaskRunMemoryDiagnosis from './TaskRunMemoryDiagnosis';
import styles from './BodyForm.less';

interface SchedulingConfigProps {
  initTaskDefinition?: TaskDefinition;
  taskTemplate?: TaskTemplate;
  form: FormInstance;
}

/** TODO: abstract a factory function for differen custom label element
 * Render custom label
 * */
const renderLabel = (displayParam: DisplayParameter, taskDefinition?: TaskDefinition) => {
  const { displayName = '' } = displayParam || {};
  if (displayName.toLowerCase().includes('spark configuration')) {
    return (
      <div>
        <div>{displayName}:</div>
        <TaskRunMemoryDiagnosis taskDefId={`${taskDefinition?.id}`} />
      </div>
    );
  }
  return displayName;
};

const shouldLabelColonHidden = (displayParam: DisplayParameter) => {
  return displayParam?.displayName?.toLocaleLowerCase()?.includes?.('spark configuration');
};

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
            label: renderLabel(displayParam, initTaskDefinition),
            colon: !shouldLabelColonHidden(displayParam),
          }}
        />
      </Row>
    );
  });

  return <div className={styles.ParamConfig}>{paramItems}</div>;
};
