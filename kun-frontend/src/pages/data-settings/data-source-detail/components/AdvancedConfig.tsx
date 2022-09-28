import React, { memo, useMemo, useState, useEffect } from 'react';
import { Button, Form, Input, Select, Row, Col, Space, Radio, Modal } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { FormInstance } from 'antd/es/form';
import { DataSourceType, hiveConnections, hiveConnectionTemplateList } from '@/definitions/DataSource.type';
import useI18n from '@/hooks/useI18n';
import { useUpdateEffect } from 'ahooks';
import styles from './AdvancedConfig.less';

interface AdvancedConfigProps {
  form: FormInstance;
  isModalOpen: boolean;
  setIsModalOpen: (value: boolean) => void;
}

export const AdvancedConfig = memo((props: AdvancedConfigProps) => {
  const t = useI18n();
  const [refresh, setRefresh] = useState(false);
  const { isModalOpen, setIsModalOpen, form } = props;
  const [AdvancedConfigForm] = Form.useForm();
  const userConnectionList = Form.useWatch(['users', 'datasourceConnection', 'userConnectionList'], form);

  const submit = () => {
    AdvancedConfigForm.validateFields().then(values => {
      console.log(values);
    });
  };

  const onCancel = () => {
    AdvancedConfigForm.resetFields();
    setIsModalOpen(false);
  };
  return (
    <Modal
      title="高级配置"
      open={isModalOpen}
      width='50%'
      onOk={submit}
      onCancel={onCancel}
    >
      <Form
        form={AdvancedConfigForm}
        labelAlign="left">
        {hiveConnections.map(c => (
          c.connection !== 'userConnection' &&
          <div>
            <div className={styles.connectionTitle}>
              {t(`dataSettings.addUpdate.connectionTitle.${c.connection}`)}
            </div>
            <Row gutter={[8, 8]}>
              <Col span={22}>
                <Form.Item
                  labelCol={{ span: 4 }}
                  name={[c.connection, 'connectionConfigInfo', 'connectionType']}
                  label="连接方式"
                  rules={[{ required: true }]}>
                  <Radio.Group
                    onChange={() => setRefresh(!refresh)}
                  >
                    {c.templateList.map(template => (
                      <Radio
                        key={template}
                        value={template}
                      >
                        {t(`dataSettings.addUpdate.connectionTypeOption.${template}`)}
                      </Radio>
                    ))}
                  </Radio.Group>
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={[8, 8]}>
              {hiveConnectionTemplateList.find(item => item.tempType === AdvancedConfigForm.getFieldValue([c.connection, 'connectionConfigInfo', 'connectionType']))?.fieldList?.map(field => (
                <Col
                  span={11}
                  style={{ marginRight: '12px' }}>
                  <Form.Item
                    labelCol={{ span: 8 }}
                    name={[field.field]}
                    label={t(`dataSettings.field.${field.field}`)}
                    rules={[{ required: true }]}>
                    <Input />
                  </Form.Item>
                </Col>
              ))}
            </Row>
          </div>
        ))}

      </Form>
    </Modal>
  );
});