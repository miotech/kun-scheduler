import React, { memo, useMemo, useState, useCallback } from 'react';
import { Button, Form, Input, Row, Col, Radio, Modal } from 'antd';
import { FormInstance } from 'antd/es/form';
import {
  DataSourceType,
  FieldListMap,
  HiveTempType,
  databaseTemplateMap,
  DataSourceInfo,
} from '@/definitions/DataSource.type';
import useI18n from '@/hooks/useI18n';
import { useUpdateEffect } from 'ahooks';
import styles from './AdvancedConfig.less';

interface AdvancedConfigProps {
  form: FormInstance;
  isEdit: boolean;
  AdvancedConfigForm: FormInstance;
  advancedConfigFormData: Object;
  isModalOpen: boolean;
  dataSourceInfo: DataSourceInfo;
  setIsModalOpen: (value: boolean) => void;
  setAdvancedConfigFormData: (value: Object) => void;
}

export const AdvancedConfig = memo((props: AdvancedConfigProps) => {
  const t = useI18n();
  const [refresh, setRefresh] = useState(false);
  const {
    isModalOpen,
    isEdit,
    setIsModalOpen,
    dataSourceInfo,
    form,
    AdvancedConfigForm,
    advancedConfigFormData,
    setAdvancedConfigFormData,
  } = props;
  const datasourceType = Form.useWatch('datasourceType', form);
  const userConnectionList = Form.useWatch(['datasourceConnection', 'userConnectionList'], form);

  const connectionList = useMemo(() => {
    return databaseTemplateMap[datasourceType as DataSourceType];
  }, [datasourceType]);

  const submit = () => {
    AdvancedConfigForm.validateFields().then(values => {
      const { dataConnection, metadataConnection, storageConnection } = advancedConfigFormData;
      const newAdvancedConfigFormData = {
        dataConnection: { ...dataConnection, ...values.dataConnection },
        metadataConnection: { ...metadataConnection, ...values.metadataConnection },
        storageConnection: { ...storageConnection, ...values.storageConnection },
      };
      setAdvancedConfigFormData(newAdvancedConfigFormData);
      setIsModalOpen(false);
    });
  };

  const onCancel = () => {
    AdvancedConfigForm.resetFields();
    setIsModalOpen(false);
  };

  const copyConfig = useCallback(
    (connection, connectionConfigInfo) => {
      const formValues = AdvancedConfigForm.getFieldsValue();
      formValues[connection].connectionConfigInfo = connectionConfigInfo;
      AdvancedConfigForm.setFieldsValue(formValues);
      setRefresh(!refresh);
    },
    [refresh, AdvancedConfigForm],
  );

  useUpdateEffect(() => {
    if (datasourceType !== DataSourceType.HIVE) {
      AdvancedConfigForm.setFieldValue(['dataConnection', 'connectionConfigInfo', 'connectionType'], datasourceType);
      AdvancedConfigForm.setFieldValue(
        ['metadataConnection', 'connectionConfigInfo', 'connectionType'],
        datasourceType,
      );
      AdvancedConfigForm.setFieldValue(['storageConnection', 'connectionConfigInfo', 'connectionType'], datasourceType);
    }
  }, [datasourceType]);

  return (
    <Modal title={t('dataSettings.advancedConfig')} open={isModalOpen} width="60%" onOk={submit} onCancel={onCancel}>
      <Form form={AdvancedConfigForm} initialValues={advancedConfigFormData} labelAlign="left">
        {connectionList?.map(
          c =>
            c.connection !== 'userConnection' && (
              <div>
                <Row>
                  <Col span={22} className={styles.connectionHeader}>
                    <Col span={5} className={styles.connectionTitle}>
                      {t(`dataSettings.addUpdate.connectionTitle.${c.connection}`)}
                    </Col>
                    {isEdit &&
                      userConnectionList?.map((item, index) => (
                        <Button
                          type="link"
                          onClick={() => copyConfig(c.connection, item.connectionConfigInfo)}
                          style={{ paddingLeft: 0 }}
                        >
                          {t('dataSettings.connectConfig.copy', { index: index + 1 })}
                        </Button>
                      ))}
                  </Col>
                </Row>
                <Row gutter={[8, 8]}>
                  <Col span={22}>
                    {isEdit && (
                      <Form.Item
                        labelCol={{ span: 5 }}
                        name={[c.connection, 'connectionConfigInfo', 'connectionType']}
                        label={t('dataSettings.connectConfig.type')}
                        rules={[{ required: true }]}
                        hidden={c.templateList?.length === 1}
                      >
                        <Radio.Group onChange={() => setRefresh(!refresh)}>
                          {c.templateList.map(template => (
                            <Radio key={template} value={template}>
                              {t(`dataSettings.addUpdate.connectionTypeOption.${template}`)}
                            </Radio>
                          ))}
                        </Radio.Group>
                      </Form.Item>
                    )}
                    {!isEdit && (
                      <Form.Item
                        labelCol={{ span: 5 }}
                        name={[c.connection, 'connectionConfigInfo', 'connectionType']}
                        label={t('dataSettings.connectConfig.type')}
                        rules={[{ required: true }]}
                        hidden={c.templateList?.length === 1}
                      >
                        <span>
                          {t(
                            `dataSettings.addUpdate.connectionTypeOption.${
                              dataSourceInfo?.datasourceConnection[c.connection].connectionConfigInfo?.connectionType
                            }`,
                          )}
                        </span>
                      </Form.Item>
                    )}
                  </Col>
                </Row>
                <Row gutter={[8, 8]}>
                  {FieldListMap[
                    AdvancedConfigForm.getFieldValue([
                      c.connection,
                      'connectionConfigInfo',
                      'connectionType',
                    ]) as HiveTempType
                  ]?.map(field => (
                    <Col span={11} style={{ marginRight: '12px' }}>
                      {isEdit && (
                        <Form.Item
                          labelCol={{ span: 10 }}
                          name={[c.connection, 'connectionConfigInfo', field.field]}
                          label={t(`dataSettings.field.${field.field}`)}
                          rules={[{ required: field.required }]}
                        >
                          <Input autoComplete="off" />
                        </Form.Item>
                      )}
                      {!isEdit && (
                        <Form.Item
                          labelCol={{ span: 10 }}
                          name={[c.connection, 'connectionConfigInfo', field.field]}
                          label={t(`dataSettings.field.${field.field}`)}
                          rules={[{ required: field.required }]}
                        >
                          <span>
                            {field.encryption
                              ? '***'
                              : dataSourceInfo?.datasourceConnection[c.connection].connectionConfigInfo?.[field.field]}
                          </span>
                        </Form.Item>
                      )}
                    </Col>
                  ))}
                </Row>
              </div>
            ),
        )}
      </Form>
    </Modal>
  );
});
