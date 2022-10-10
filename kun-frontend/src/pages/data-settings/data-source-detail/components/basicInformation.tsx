import React, { memo, useCallback, useMemo } from 'react';
import { Button, Form, Input, Select, Row, Col, Space, Message } from 'antd';
import { FormInstance } from 'antd/es/form';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { addDatabaseService, updateDatabaseService } from '@/services/dataSettings';
import { useRequest } from 'ahooks';
import { dataSourceList, DataSourceType, DataSourceInfo } from '@/definitions/DataSource.type';
import { dateFormatter } from '@/utils/datetime-utils';
import { useHistory } from 'umi';
import styles from './basicInformation.less';

interface BasicInformationProps {
  form: FormInstance;
  advancedConfigFormData: Object;
  isEditing: boolean;
  isCreate: boolean;
  dataSourceInfo: DataSourceInfo;
  queryDatabaseService: () => void;
  setIsEditing: (isEditing: boolean) => void;
}

const { Option } = Select;

export const BasicInformation = memo((props: BasicInformationProps) => {
  const history = useHistory();
  const {
    form,
    advancedConfigFormData,
    isEditing,
    isCreate,
    setIsEditing,
    dataSourceInfo,
    queryDatabaseService,
  } = props;

  const t = useI18n();
  const { selector } = useRedux(state => ({
    allTagList: state.dataDiscovery.allTagList,
    username: state.user.username,
  }));

  const datasourceType = Form.useWatch('datasourceType', form);

  const { loading, runAsync: addDatabaseServiceRun } = useRequest(addDatabaseService, {
    debounceWait: 300,
    manual: true,
  });

  const { loading: updatedLoading, runAsync: updateDatabaseServiceRun } = useRequest(updateDatabaseService, {
    debounceWait: 300,
    manual: true,
  });

  const submitForm = useCallback(() => {
    form.validateFields().then(async values => {
      const formValues = values;
      formValues.datasourceConnection = {
        ...advancedConfigFormData,
        userConnectionList: formValues.datasourceConnection?.userConnectionList,
      };
      if (isCreate) {
        const res = await addDatabaseServiceRun(formValues);
        if (res) {
          Message.success(t('dataSettings.save.success'));
          history.replace(`/settings/data-source/detail?id=${res.id}`);
        }
      } else {
        const res = await updateDatabaseServiceRun({ id: dataSourceInfo.id, ...formValues });
        if (res) {
          Message.success(t('dataSettings.save.success'));
          await queryDatabaseService();
          setIsEditing(false);
        }
      }
    });
  }, [
    form,
    advancedConfigFormData,
    history,
    addDatabaseServiceRun,
    updateDatabaseServiceRun,
    dataSourceInfo,
    isCreate,
    setIsEditing,
    queryDatabaseService,
    t,
  ]);

  const isEdit = useMemo(() => {
    return (isEditing && dataSourceInfo?.createUser === selector?.username) || isCreate;
  }, [isEditing, isCreate, selector?.username, dataSourceInfo?.createUser]);

  const changeDatasourceType = useCallback(
    value => {
      const userConnectionList = form.getFieldValue(['datasourceConnection', 'userConnectionList']);
      if (!userConnectionList) {
        const newValues = [
          {
            connectionConfigInfo: {
              connectionType: value,
              securityUserList: [],
            },
          },
        ];
        form.setFieldValue(['datasourceConnection', 'userConnectionList'], newValues);
      }
    },
    [form],
  );

  const cancel = useCallback(() => {
    setIsEditing(false);
    form.setFieldsValue(dataSourceInfo);
  }, [form, setIsEditing, dataSourceInfo]);

  return (
    <Form form={form} className={styles.container} labelCol={{ span: 7 }}>
      <div className={styles.header}>
        <div className={styles.title}>{t('dataSettings.basicInfo')}</div>
        <div className={styles.buttonGroup}>
          <Space size="small">
            {isEditing && (
              <Button type="primary" onClick={cancel}>
                {t('common.button.cancel')}
              </Button>
            )}
            {!isEditing && !isCreate && (
              <Button type="primary" onClick={() => setIsEditing(true)}>
                {t('common.button.edit')}
              </Button>
            )}
            <Button type="primary" loading={loading || updatedLoading} onClick={submitForm}>
              {t('common.button.save')}
            </Button>
          </Space>
        </div>
      </div>
      <div className={styles.information}>
        <Row gutter={[8, 8]}>
          <Col span={8}>
            {isEdit && (
              <Form.Item name="name" label={t('dataSettings.addUpdate.name')} rules={[{ required: true }]}>
                <Input autoComplete="off" />
              </Form.Item>
            )}
            {!isEdit && (
              <Form.Item name="name" label={t('dataSettings.addUpdate.name')} rules={[{ required: true }]}>
                <span className={styles.value}>{dataSourceInfo?.name}</span>
              </Form.Item>
            )}
          </Col>
          <Col span={8}>
            {isEdit && (
              <Form.Item name="datasourceType" label={t('dataSettings.addUpdate.dbType')} rules={[{ required: true }]}>
                <Select style={{ width: '100%' }} disabled={!isEdit} onChange={changeDatasourceType}>
                  {dataSourceList.map(type => (
                    <Option key={type} value={type}>
                      {type}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            )}
            {!isEdit && (
              <Form.Item name="datasourceType" label={t('dataSettings.addUpdate.dbType')} rules={[{ required: true }]}>
                <span className={styles.value}>{dataSourceInfo?.datasourceType}</span>
              </Form.Item>
            )}
          </Col>
          <Col span={8}>
            {isEdit && (
              <Form.Item name="tags" label={t('dataSettings.addUpdate.tags')}>
                <Select style={{ width: '100%' }} mode="tags" disabled={!isEdit}>
                  {selector.allTagList.map(tag => (
                    <Option key={tag} value={tag}>
                      {tag}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            )}
            {!isEdit && (
              <Form.Item name="tags" label={t('dataSettings.addUpdate.tags')}>
                <span className={styles.value}>{dataSourceInfo?.tags}</span>
              </Form.Item>
            )}
          </Col>
        </Row>
        {datasourceType === DataSourceType.HIVE && (
          <Row gutter={[8, 8]}>
            <Col span={8}>
              {isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'host']} label={t('dataSettings.field.datastoreHost')}>
                  <Input disabled={!isEdit} />
                </Form.Item>
              )}
              {!isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'host']} label={t('dataSettings.field.datastoreHost')}>
                  <span className={styles.value}>{dataSourceInfo?.datasourceConfigInfo?.host}</span>
                </Form.Item>
              )}
            </Col>
            <Col span={8}>
              {isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'port']} label={t('dataSettings.field.datastorePort')}>
                  <Input disabled={!isEdit} />
                </Form.Item>
              )}
              {!isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'port']} label={t('dataSettings.field.datastorePort')}>
                  <span className={styles.value}>{dataSourceInfo?.datasourceConfigInfo?.port}</span>
                </Form.Item>
              )}
            </Col>
            <Col span={8}>
              {isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'athenaUrl']} label={t('dataSettings.field.athenaServerUrl')}>
                  <Input disabled={!isEdit} />
                </Form.Item>
              )}
              {!isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'athenaUrl']} label={t('dataSettings.field.athenaServerUrl')}>
                  <span className={styles.value}>{dataSourceInfo?.datasourceConfigInfo?.athenaUrl}</span>
                </Form.Item>
              )}
            </Col>
          </Row>
        )}
        {datasourceType && datasourceType !== DataSourceType.HIVE && (
          <Row gutter={[8, 8]}>
            <Col span={8}>
              {isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'host']} label={t('dataSettings.field.host')}>
                  <Input disabled={!isEdit} />
                </Form.Item>
              )}
              {!isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'host']} label={t('dataSettings.field.host')}>
                  <span className={styles.value}>{dataSourceInfo?.datasourceConfigInfo?.host}</span>
                </Form.Item>
              )}
            </Col>
            <Col span={8}>
              {isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'port']} label={t('dataSettings.field.port')}>
                  <Input disabled={!isEdit} />
                </Form.Item>
              )}
              {!isEdit && (
                <Form.Item name={['datasourceConfigInfo', 'port']} label={t('dataSettings.field.port')}>
                  <span className={styles.value}>{dataSourceInfo?.datasourceConfigInfo?.port}</span>
                </Form.Item>
              )}
            </Col>
          </Row>
        )}
        {dataSourceInfo && (
          <Row gutter={[8, 8]}>
            <Col span={8}>
              <Form.Item label={t('dataSettings.addUpdate.createUser')}>
                <span className={styles.value}>{dataSourceInfo?.createUser}</span>
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label={t('dataSettings.addUpdate.createTime')}>
                <span className={styles.value}>{dateFormatter(dataSourceInfo?.createTime)}</span>
              </Form.Item>
            </Col>
          </Row>
        )}
      </div>
    </Form>
  );
});
