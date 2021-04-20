import React, { useEffect, useState } from 'react';
import { Col, Divider, Row, Form, Radio, Button } from 'antd';
import { PlusOutlined, CloseOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';

import { FormInstance } from 'antd/es/form';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { FormListFieldData } from 'antd/es/form/FormList';
import { FormListOperation } from 'antd/lib/form/FormList';

import { CronExpressionInput } from '@/components/CronExpressionInput';
import { DatasetSearchSelector } from '@/components/DatasetSearchSelector';
import { TaskSearchSelector } from '@/components/TaskSearchSelector';
import { OutputDatasetField } from '@/components/OutputDatasetField';

import LogUtils from '@/utils/logUtils';
import { getTaskDefinitionsFromFlattenedProps } from '@/utils/transformDataset';
import { validateQuartzCron } from '@/utils/cronUtils';
import { OneshotDatePicker } from '@/pages/data-development/task-definition-config/components/OneshotDatePicker';

import styles from './BodyForm.less';

interface SchedulingConfigProps {
  initTaskDefinition?: TaskDefinition;
  form: FormInstance;
}

const formItemLayout = {
  labelCol: {
    span: 6,
  },
  wrapperCol: {
    span: 18,
  },
};
const formItemLayoutWithOutLabel = {
  wrapperCol: {
    span: 18,
    offset: 6,
  },
};

const logger = LogUtils.getLoggers('SchedulingConfig');

export const SchedulingConfig: React.FC<SchedulingConfigProps> = function SchedulingConfig(props) {
  const t = useI18n();

  const { form, initTaskDefinition } = props;

  useEffect(() => {
    /* Automatically set output dataset field */
    form.setFieldsValue({
      taskPayload: {
        scheduleConfig: {
          outputDatasets: initTaskDefinition?.taskPayload?.scheduleConfig.outputDatasets || [],
        },
      },
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initTaskDefinition]);

  const [upstreamType, setUpstreamType] = useState<'inputDataset' | 'searchTaskDef'>(
    initTaskDefinition?.taskPayload?.scheduleConfig?.inputDatasets?.length === 0 &&
      initTaskDefinition?.taskPayload?.scheduleConfig?.inputNodes?.length > 0
      ? 'searchTaskDef'
      : 'inputDataset',
  );

  const renderOutputDatasetFields = (fields: FormListFieldData[], { add, remove }: FormListOperation) => {
    logger.debug('fields = %o', fields);
    return (
      <div data-tid="output-dataset-fields">
        {fields.map((field, idx) => {
          return (
            <Form.Item
              {...(idx === 0 ? formItemLayout : formItemLayoutWithOutLabel)}
              label={idx === 0 ? t('dataDevelopment.definition.scheduleConfig.output.outputDataset') : ''}
              required={false}
              key={field.key}
            >
              <Row>
                <Col flex="1 1">
                  <Form.Item
                    {...field}
                    // name={['taskPayload', 'scheduleConfig', 'outputDatasets', field.name]}
                    initialValue={
                      initTaskDefinition?.taskPayload?.scheduleConfig?.outputDatasets?.[field.key] ?? undefined
                    }
                    noStyle
                  >
                    <OutputDatasetField />
                  </Form.Item>
                </Col>
                <Col flex="0 0 60px">
                  <Button
                    type="link"
                    onClick={() => {
                      remove(field.name);
                    }}
                  >
                    <CloseOutlined />
                  </Button>
                </Col>
              </Row>
            </Form.Item>
          );
        })}
        {/* Add button */}
        <Form.Item
          label={fields.length === 0 ? t('dataDevelopment.definition.scheduleConfig.output.outputDataset') : ''}
          {...(fields.length === 0 ? formItemLayout : formItemLayoutWithOutLabel)}
        >
          <Button
            type="dashed"
            onClick={() => {
              add();
            }}
            style={{ width: 'calc(100% - 60px)' }}
          >
            <PlusOutlined />
            <span>{t('common.button.add')}</span>
          </Button>
        </Form.Item>
      </div>
    );
  };

  const scheduleInputRenderer = (formInstance: FormInstance<any>) => {
    const { getFieldValue } = formInstance;
    const selectedScheduleType = getFieldValue(['taskPayload', 'scheduleConfig', 'type']);

    // case scheduled:
    if (selectedScheduleType === 'SCHEDULED') {
      return (
        <Form.Item
          label={t('dataDevelopment.definition.scheduleConfig.cronExpression')}
          name={['taskPayload', 'scheduleConfig', 'cronExpr']}
          rules={[
            { required: true },
            {
              validator(rule, value) {
                if (validateQuartzCron(value)) {
                  return Promise.resolve();
                }
                // else
                return Promise.reject(t('common.cronstrue.invalidCronExp'));
              },
            },
          ]}
          initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.cronExpr}
        >
          <CronExpressionInput hideErrorAlert />
        </Form.Item>
      );
    }
    // case one-shot:
    if (selectedScheduleType === 'ONESHOT') {
      return (
        <Form.Item
          label={t('scheduledTasks.property.oneShotExecutionTime')}
          name={['taskPayload', 'scheduleConfig', 'cronExpr']}
          initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.cronExpr}
        >
          <OneshotDatePicker
            style={{ width: '240px' }}
            placeholder={t('scheduledTasks.property.oneShotExecutionTimePlaceholder')}
          />
        </Form.Item>
      );
    }
    // case NONE: render nothing
    // else
    return null;
  };

  return (
    <div className={styles.SchedulingConfig}>
      <section data-tid="time-scheduling">
        <Row>
          <Col flex="0 0 120px">
            <span className={styles.sectionLabel}>
              {/* Timing Config */}
              {t('dataDevelopment.definition.scheduleConfig.timing')}
            </span>
          </Col>
          <Col flex="1 1">
            <Row>
              {/* Schedule Type */}
              <Form.Item
                label={t('dataDevelopment.definition.scheduleConfig.scheduleType')}
                name={['taskPayload', 'scheduleConfig', 'type']}
                rules={[{ required: true }]}
                initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.type}
              >
                <Radio.Group>
                  <Radio value="ONESHOT">
                    <span className={styles.RadioBtn}>
                      {t('dataDevelopment.definition.scheduleConfig.scheduleType.oneShot')}
                    </span>
                  </Radio>
                  <Radio value="SCHEDULED">
                    <span className={styles.RadioBtn}>
                      {t('dataDevelopment.definition.scheduleConfig.scheduleType.scheduled')}
                    </span>
                  </Radio>
                  <Radio value="NONE">
                    <span className={styles.RadioBtn}>
                      {t('dataDevelopment.definition.scheduleConfig.scheduleType.manual')}
                    </span>
                  </Radio>
                </Radio.Group>
              </Form.Item>
            </Row>
            <Row>
              {/* Cron Expression */}
              <Form.Item noStyle shouldUpdate={() => true}>
                {scheduleInputRenderer}
              </Form.Item>
            </Row>
          </Col>
        </Row>
      </section>
      <Divider />
      <section data-tid="upstream-config">
        <Row>
          <Col flex="0 0 120px">
            <span className={styles.sectionLabel}>
              {/* Upstream Config */}
              {t('dataDevelopment.definition.scheduleConfig.upstream')}
            </span>
          </Col>
          <Col flex="1 1">
            <Row>
              {/* Upstream type */}
              <Form.Item label={t('dataDevelopment.definition.scheduleConfig.upstream.type')} required>
                <Radio.Group
                  value={upstreamType}
                  onChange={ev => {
                    setUpstreamType(ev.target.value);
                  }}
                >
                  <Radio value="inputDataset">
                    <span className={styles.RadioBtn}>
                      {t('dataDevelopment.definition.scheduleConfig.upstream.type.inputDataset')}
                    </span>
                  </Radio>
                  <Radio value="searchTaskDef">
                    <span className={styles.RadioBtn}>
                      {t('dataDevelopment.definition.scheduleConfig.upstream.type.search')}
                    </span>
                  </Radio>
                </Radio.Group>
              </Form.Item>
            </Row>
            {/* Upstream data input config */}
            <Row>
              {(() => {
                if (upstreamType === 'inputDataset') {
                  return (
                    <Form.Item
                      name={['taskPayload', 'scheduleConfig', 'inputDatasets']}
                      label={t('dataDevelopment.definition.scheduleConfig.upstream.type.inputDataset')}
                      initialValue={getTaskDefinitionsFromFlattenedProps(
                        initTaskDefinition?.taskPayload?.scheduleConfig?.inputDatasets || [],
                        initTaskDefinition?.upstreamTaskDefinitions || [],
                      )}
                    >
                      <DatasetSearchSelector />
                    </Form.Item>
                  );
                }
                return (
                  <Form.Item
                    name={['taskPayload', 'scheduleConfig', 'inputNodes']}
                    label={t('dataDevelopment.definition.scheduleConfig.upstream.type.search')}
                    initialValue={(initTaskDefinition?.upstreamTaskDefinitions || []).map(taskDef => ({
                      value: taskDef.id,
                      label: taskDef.name,
                    }))}
                  >
                    <TaskSearchSelector />
                  </Form.Item>
                );
              })()}
            </Row>
          </Col>
        </Row>
      </section>
      <Divider />
      {/* data output config */}
      <section data-tid="output-config">
        <Row>
          <Col flex="0 0 120px">
            <span className={styles.sectionLabel}>
              {/* Output Config */}
              {t('dataDevelopment.definition.scheduleConfig.output')}
            </span>
          </Col>
          <Col flex="1 1">
            {/* List of output dataset fields */}
            <Form.List name={['taskPayload', 'scheduleConfig', 'outputDatasets']}>
              {renderOutputDatasetFields}
            </Form.List>
          </Col>
        </Row>
      </section>
    </div>
  );
};
