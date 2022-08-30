import React, { useRef } from 'react';
import { Form, Tabs } from 'antd';
import useI18n from '@/hooks/useI18n';
import find from 'lodash/find';
import isEqual from 'lodash/isEqual';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { FormInstance } from 'antd/es/form';
import { FieldData } from 'rc-field-form/lib/interface';

import { ParamConfig } from '@/pages/data-development/task-definition-config/components/ParamConfig';
import { SchedulingConfig } from '@/pages/data-development/task-definition-config/components/ScheduingConfig';
import useRedux from '@/hooks/useRedux';
import { NotificationConfig } from '@/pages/data-development/task-definition-config/components/NotificationConfig';
import { useSize } from 'ahooks';
import { VersionHistory } from './VersionHisotry';
import TaskDag from './task-dag';
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

export const BodyForm: React.FC<BodyFormProps> = function TaskDefinitionBodyForm(props) {
  const t = useI18n();
  const { dispatch } = useRedux(() => ({}));
  const ref = useRef(null);
  const size = useSize(ref);
  const { initTaskDefinition, taskTemplate, form } = props;

  return (
    <div ref={ref}>
      <Form
        className={styles.BodyForm}
        form={form}
        onFieldsChange={(changedFields: FieldData[]) => {
          dispatch.dataDevelopment.setDefinitionFormDirty(true);
          /* Reset the other field when one of
          [taskPayload.scheduleConfig.inputDatasets, taskPayload.scheduleConfig.inputNodes] is changed. */
          if (find(changedFields, field => isEqual(field.name, ['taskPayload', 'scheduleConfig', 'inputDatasets']))) {
            form.setFields([
              {
                name: ['taskPayload', 'scheduleConfig', 'inputNodes'],
                value: [],
              },
            ]);
          } else if (
            find(changedFields, field => isEqual(field.name, ['taskPayload', 'scheduleConfig', 'inputNodes']))
          ) {
            form.setFields([
              {
                name: ['taskPayload', 'scheduleConfig', 'inputDatasets'],
                value: [],
              },
            ]);
          }
        }}
        {...formLayout}
      >
        <Tabs>
          {/* Task execution parameters */}
          <Tabs.TabPane tab={t('dataDevelopment.definition.paramConfig')} key="paramConfig" forceRender>
            <ParamConfig form={form} taskTemplate={taskTemplate} initTaskDefinition={initTaskDefinition} />
          </Tabs.TabPane>
          {/* Schedule config */}
          <Tabs.TabPane tab={t('dataDevelopment.definition.scheduleConfig')} key="scheduleConfig" forceRender>
            <SchedulingConfig form={form} initTaskDefinition={initTaskDefinition} />
          </Tabs.TabPane>
          {/* Notification rules config */}
          <Tabs.TabPane tab={t('dataDevelopment.definition.notificationConfig')} key="notificationConfig" forceRender>
            <NotificationConfig
              form={form}
              initNotificationWhen={initTaskDefinition?.taskPayload?.notifyConfig?.notifyWhen || null}
              initUserNotificationConfigItems={initTaskDefinition?.taskPayload?.notifyConfig?.notifierConfig || null}
              initTaskDefinition={initTaskDefinition}
            />
          </Tabs.TabPane>
          {/* Version history */}
          <Tabs.TabPane tab={t('dataDevelopment.definition.versionHistory')} key="versionHistory" forceRender>
            <VersionHistory taskCommits={initTaskDefinition?.taskCommits || []} />
          </Tabs.TabPane>

          {/* task dag */}
          <Tabs.TabPane tab={t('dataDevelopment.definition.dag')} key="taskDag" forceRender>
            <TaskDag task={initTaskDefinition} width={size?.width} height={600} />
          </Tabs.TabPane>
        </Tabs>
      </Form>
    </div>
  );
};
