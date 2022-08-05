import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Col, Divider, Row, Form, Radio, Button, Select, InputNumber } from 'antd';
import { PlusOutlined, CloseOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';

import { FormInstance } from 'antd/es/form';
import { TaskDefinition, BlockType, ResourceQueues } from '@/definitions/TaskDefinition.type';
import { FormListFieldData } from 'antd/es/form/FormList';
import { FormListOperation } from 'antd/lib/form/FormList';

import { CronExpressionInput } from '@/components/CronExpressionInput';
import { DatasetSearchSelector } from '@/components/DatasetSearchSelector';
import { TaskSearchSelector } from '@/components/TaskSearchSelector';
import { OutputDatasetField } from '@/components/OutputDatasetField';

import LogUtils from '@/utils/logUtils';
import { getTaskDefinitionsFromFlattenedProps } from '@/utils/transformDataset';
import { OneshotDatePicker } from '@/pages/data-development/task-definition-config/components/OneshotDatePicker';
import { parse } from '@joshoy/quartz-cron-parser';
import RetryCheckBox from '@/components/RetryCheckBox/RetryCheckBox';

import { fetchExecutorInfo } from '@/services/data-development/task-definitions';
import { useRequest, useMount } from 'ahooks';
import timeZoneMapList from './timezoneMap';

import styles from './BodyForm.less';

interface SchedulingConfigProps {
  initTaskDefinition?: TaskDefinition;
  form: FormInstance;
}

const { Option } = Select;

const getDefaultTimeZone = () => {
  const offsetRate = -(new Date().getTimezoneOffset() / 60);
  const timeZoneDefaultValue = timeZoneMapList.find(i => i.offset === offsetRate)?.value ?? 'UTC';
  return timeZoneDefaultValue;
};

const defaultTimeZone = getDefaultTimeZone();

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
  const [resourceQueuesOptions, setResourceQueuesOptions] = useState<ResourceQueues[] | undefined>([]);

  const { data: executorInfo, loading, run: doSearch } = useRequest(fetchExecutorInfo, {
    manual: true,
  });

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

  const [isCheckedRetry, setIsCheckedRetry] = useState(
    !!form.getFieldValue(['taskPayload', 'scheduleConfig', 'retries']),
  );
  useEffect(() => {
    setIsCheckedRetry(!!form.getFieldValue(['taskPayload', 'scheduleConfig', 'retries']));
  }, [form]);
  const handleChangeRetry = useCallback(v => {
    setIsCheckedRetry(!!v);
  }, []);

  const blockOptions = useMemo(
    () => [
      {
        label: t('dataDevelopment.definition.scheduleConfig.blockConfig.NONE'),
        value: BlockType.NONE,
      },
      {
        label: t('dataDevelopment.definition.scheduleConfig.blockConfig.WAIT_PREDECESSOR'),
        value: BlockType.WAIT_PREDECESSOR,
      },
      {
        label: t('dataDevelopment.definition.scheduleConfig.blockConfig.WAIT_PREDECESSOR_DOWNSTREAM'),
        value: BlockType.WAIT_PREDECESSOR_DOWNSTREAM,
      },
    ],
    [t],
  );

  useMount(() => {
    doSearch();
  });

  const executorLabelOptions = useMemo(() => {
    let executorLabel: string[] = [];
    executorInfo?.forEach(item => {
      executorLabel = executorLabel.concat(item.labels);
    });
    return executorLabel;
  }, [executorInfo]);

  const changeExecutorLabel = useCallback(
    value => {
      const findItem = executorInfo?.find(item => item.labels.includes(value));
      setResourceQueuesOptions(findItem?.resourceQueues);
      form.setFieldsValue({
        taskPayload: {
          scheduleConfig: {
            queueName: null,
          },
        },
      });
    },
    [executorInfo, form],
  );

  useEffect(() => {
    const findItem = executorInfo?.find(item =>
      item.labels.includes(form.getFieldValue(['taskPayload', 'scheduleConfig', 'executorLabel'])),
    );
    setResourceQueuesOptions(findItem?.resourceQueues);
  }, [form, executorInfo]);

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
        <Form.Item label={t('dataDevelopment.definition.scheduleConfig.cronExpression')}>
          <Form.Item
            style={{ display: 'inline-block', width: 180, marginRight: 20 }}
            name={['taskPayload', 'scheduleConfig', 'timeZone']}
            rules={[{ required: true, message: t('dataDevelopment.definition.scheduleConfig.timeZone') }]}
            initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.timeZone ?? defaultTimeZone}
          >
            <Select
              placeholder={t('dataDevelopment.definition.scheduleConfig.timeZone')}
              style={{ width: '100%' }}
              dropdownMatchSelectWidth={false}
              showSearch
              optionFilterProp="children"
            >
              {timeZoneMapList.map(i => (
                <Option key={i.value} value={i.value}>
                  {i.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            style={{ display: 'inline-block', width: 'calc(100% - 220px)' }}
            name={['taskPayload', 'scheduleConfig', 'cronExpr']}
            rules={[
              { required: true },
              () => ({
                validator(_, quartzCronValue) {
                  // when expression is empty, `required` rule will be triggered.
                  if (!`${quartzCronValue}`.trim()) {
                    return Promise.resolve();
                  }
                  // for other cases
                  const expr = parse(quartzCronValue as string);
                  if (expr.error) {
                    let reason = expr.error.message;
                    if (reason.match(/Syntax error at/g) || reason.match(/Cannot read property/g)) {
                      reason =
                        'Invalid cron expression. See: https://www.freeformatter.com/cron-expression-generator-quartz.html for help.';
                    }
                    return Promise.reject(reason);
                  }
                  if (expr.result[0].mode !== 'specific' || expr.result[0].value !== 0) {
                    // eslint-disable-next-line prefer-promise-reject-errors
                    return Promise.reject(
                      t('dataDevelopment.definition.scheduleConfig.cronExpression.alertSecondsBeZero'),
                    );
                  }
                  // else
                  return Promise.resolve();
                },
              }),
            ]}
            initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.cronExpr || '0 0 0 * * ? *'}
          >
            <CronExpressionInput hideErrorAlert />
          </Form.Item>
        </Form.Item>
      );
    }
    // case one-shot:
    if (selectedScheduleType === 'ONESHOT') {
      return (
        <Form.Item label={t('scheduledTasks.property.oneShotExecutionTime')}>
          <Form.Item
            style={{ display: 'inline-block', width: 180, marginRight: 20 }}
            name={['taskPayload', 'scheduleConfig', 'timeZone']}
            rules={[{ required: true, message: t('dataDevelopment.definition.scheduleConfig.timeZone') }]}
            initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.timeZone ?? defaultTimeZone}
          >
            <Select
              placeholder={t('dataDevelopment.definition.scheduleConfig.timeZone')}
              style={{ width: '100%' }}
              dropdownMatchSelectWidth={false}
              showSearch
              optionFilterProp="children"
            >
              {timeZoneMapList.map(i => (
                <Option key={i.value} value={i.value}>
                  {i.label}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            style={{ display: 'inline-block', width: 'calc(100% - 220px)' }}
            name={['taskPayload', 'scheduleConfig', 'cronExpr']}
            initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.cronExpr}
          >
            <OneshotDatePicker
              style={{ width: 220 }}
              placeholder={t('scheduledTasks.property.oneShotExecutionTimePlaceholder')}
            />
          </Form.Item>
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
                initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.type || 'NONE'}
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

      <Divider />
      {/* retry config */}
      <section data-tid="retry-config">
        <Row>
          <Col flex="0 0 120px">
            <span className={styles.sectionLabel}>
              {/* Output Config */}
              {t('dataDevelopment.definition.retryConfig')}
            </span>
          </Col>
          <Col flex="1 1">
            <Row>
              <Form.Item
                shouldUpdate
                label={t('dataDevelopment.definition.scheduleConfig.retryAfterFailed')}
                name={['taskPayload', 'scheduleConfig', 'retries']}
                valuePropName="value"
                initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.retries}
              >
                <RetryCheckBox onChange={handleChangeRetry} />
              </Form.Item>
            </Row>
            <Row>
              <Col flex="1 1">
                <Form.Item
                  shouldUpdate
                  label={t('dataDevelopment.definition.scheduleConfig.retireTimes')}
                  name={['taskPayload', 'scheduleConfig', 'retries']}
                  initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.retries}
                >
                  <InputNumber width={150} min={1} disabled={!isCheckedRetry} />
                </Form.Item>
              </Col>
            </Row>
            <Row>
              <Col flex="1 1">
                <Form.Item
                  shouldUpdate
                  label={t('dataDevelopment.definition.scheduleConfig.retryDelay')}
                  name={['taskPayload', 'scheduleConfig', 'retryDelay']}
                  initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.retryDelay}
                >
                  <InputNumber width={150} min={0} disabled={!isCheckedRetry} />
                </Form.Item>
              </Col>
            </Row>
          </Col>
        </Row>
      </section>
      <Divider />
      <section data-tid="block-config">
        <Row>
          <Col flex="0 0 120px">
            <span className={styles.sectionLabel}>
              {/* Output Config */}
              {t('dataDevelopment.definition.scheduleConfig.blockConfig')}
            </span>
          </Col>
          <Col flex="1 1">
            <Form.Item
              shouldUpdate
              label={t('dataDevelopment.definition.scheduleConfig.blockType')}
              name={['taskPayload', 'scheduleConfig', 'blockType']}
              valuePropName="value"
              initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.blockType ?? BlockType.NONE}
            >
              <Select style={{ width: 200 }} options={blockOptions} />
            </Form.Item>
          </Col>
        </Row>
      </section>

      <Divider />
      <section data-tid="executorLabel-config">
        <Row>
          <Col flex="0 0 120px">
            <span className={styles.sectionLabel}>
              {/* Output Config */}
              {t('dataDevelopment.definition.scheduleConfig.environment')}
            </span>
          </Col>
          <Col flex="1 1">
            <Form.Item
              shouldUpdate
              label={t('dataDevelopment.definition.scheduleConfig.executorLabel')}
              name={['taskPayload', 'scheduleConfig', 'executorLabel']}
              valuePropName="value"
              initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.executorLabel}
            >
              <Select style={{ width: 200 }} loading={loading} onChange={changeExecutorLabel} allowClear>
                {executorLabelOptions.map(item => (
                  <Option value={item}>{item}</Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>
        <Row>
          <Col flex="0 0 120px">
            <span className={styles.sectionLabel} />
          </Col>
          <Col flex="1 1">
            <Form.Item
              shouldUpdate
              label={t('dataDevelopment.definition.scheduleConfig.resourceQueues')}
              name={['taskPayload', 'scheduleConfig', 'queueName']}
              valuePropName="value"
              initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.queueName}
            >
              <Select style={{ width: 200 }} optionLabelProp="label" allowClear>
                {resourceQueuesOptions?.map(item => (
                  <Option label={item.queueName} value={item.queueName}>
                    {item.queueName}
                    <span style={{ marginLeft: 4, color: '#e0e0e0' }}>({item.workerNumbers})</span>
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>
      </section>
    </div>
  );
};
