import React, { memo, useCallback, useState, useEffect } from 'react';
import { Button, Form, Input, Select, Row, Col, Space } from 'antd';
import { FormInstance } from 'antd/es/form';
import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';
import { DataDiscoveryState } from '@/rematch/models/dataDiscovery';
import styles from './basicInformation.less';
import {
  dataSourceList,
  DataSourceType,
  HiveTempType,
  getConnectionContentMap,
  otherConnections,
  hiveConnections,
  ConnectionItem,
  getCurrentFields,
  isSame,
} from '../../components/AddUpdateDatabaseModal/fieldMap';

interface BasicInformationProps {
  form: FormInstance;
}

const { Option } = Select;

export const BasicInformation = memo((props: BasicInformationProps) => {
  const { form } = props;

  const t = useI18n();
  const { selector, dispatch } = useRedux<DataDiscoveryState>(state => state.dataDiscovery);

  const datasourceType = Form.useWatch('datasourceType', form);
  // console.log(form.getFieldsValue());
  return (
    <Form
      form={form}
      preserve={false}
      className={styles.container}
      labelCol={{ span: 7 }}
    >
      <div className={styles.header}>
        <div className={styles.title}>基本信息</div>
        <div className={styles.buttonGroup}>
          <Space size='small'>
            <Button type="primary">编辑</Button>
            <Button type="primary">保存</Button>
          </Space>
        </div>
      </div>
      <div className={styles.information}>
        <Row gutter={[8, 8]}>
          <Col span={6}>
            <Form.Item name="name" label={t('dataSettings.addUpdate.name')} rules={[{ required: true }]}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={6} >
            <Form.Item name="datasourceType" label={t('dataSettings.addUpdate.dbType')} rules={[{ required: true }]}>
              <Select
                style={{ width: '100%' }}
              >
                {dataSourceList.map(type => (
                  <Option key={type} value={type}>
                    {type}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item name="tags" label={t('dataSettings.addUpdate.tags')}>
              <Select
                style={{ width: '100%' }}
                mode="tags"
              >
                {selector.allTagList.map(tag => (
                  <Option key={tag} value={tag}>
                    {tag}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>
        {datasourceType === DataSourceType.HIVE && <Row gutter={[8, 8]}>
          <Col span={6}>
            <Form.Item name={['datasourceConfig', 'host']} label="HIVE访问主机地址" >
              <Input />
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item name={['datasourceConfig', 'port']} label="HIVE访问端口号">
              <Input />
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item name={['datasourceConfig', 'athenaUrl']} label="ATHENA访问地址" >
              <Input />
            </Form.Item>
          </Col>
        </Row>
        }
        {datasourceType && datasourceType !== DataSourceType.HIVE && <Row gutter={[8, 8]}>
          <Col span={6}>
            <Form.Item name={['datasourceConfig', 'host']} label="主机地址" >
              <Input />
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item name={['datasourceConfig', 'port']} label="端口号">
              <Input />
            </Form.Item>
          </Col>
        </Row>
        }
        {/* <Row gutter={[8, 8]}>
          <Col span={7}>
            <Form.Item name="note" label="Note" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={7}>
            <Form.Item name="note" label="Note" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
          </Col>
        </Row> */}
      </div>
    </Form>
  );
});
