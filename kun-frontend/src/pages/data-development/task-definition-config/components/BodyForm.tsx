import React from 'react';
import { Form, Tabs } from 'antd';
import useI18n from '@/hooks/useI18n';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { FormInstance } from 'antd/es/form';

import { ParamConfig } from '@/pages/data-development/task-definition-config/components/ParamConfig';
import { SchedulingConfig } from '@/pages/data-development/task-definition-config/components/ScheduingConfig';
import useRedux from '@/hooks/useRedux';

import styles from './BodyForm.less';

interface BodyFormProps extends React.ComponentProps<'div'> {
  taskTemplate: TaskTemplate;
  initTaskDefinition?: TaskDefinition;
  form: FormInstance;
}

const formLayout = {
  labelCol: { span: 6 },
  wrapperCol: { span: 18 },
};

export const BodyForm: React.FC<BodyFormProps> = props => {
  const t = useI18n();
  const { dispatch } = useRedux(() => ({}));

  const {
    initTaskDefinition,
    taskTemplate,
    form,
  } = props;

  return (
    <Form
      className={styles.BodyForm}
      form={form}
      onFieldsChange={() => {
        dispatch.dataDevelopment.setDefinitionFormDirty(true);
      }}
      {...formLayout}
    >
      <Tabs>
        <Tabs.TabPane
          tab={t('dataDevelopment.definition.paramConfig')}
          key="paramConfig"
          forceRender
        >
          <ParamConfig
            form={form}
            taskTemplate={taskTemplate}
            initTaskDefinition={initTaskDefinition}
          />
        </Tabs.TabPane>
        <Tabs.TabPane
          tab={t('dataDevelopment.definition.scheduleConfig')}
          key="scheduleConfig"
          forceRender
        >
          <SchedulingConfig
            form={form}
            initTaskDefinition={initTaskDefinition}
          />
        </Tabs.TabPane>
      </Tabs>
    </Form>
  );
};
