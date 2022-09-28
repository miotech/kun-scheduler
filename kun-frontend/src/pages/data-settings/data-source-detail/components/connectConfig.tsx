import React, { memo, useMemo, useState, useEffect } from 'react';
import { Button, Form, Input, Select, Row, Col, Space, Radio } from 'antd';
import { FormInstance } from 'antd/es/form';
import { PlusCircleOutlined, DeleteOutlined } from '@ant-design/icons';
import { HiveTempType, DataSourceType, hiveConnections, otherConnections } from '@/definitions/DataSource.type';
import { useUpdateEffect } from 'ahooks';
import styles from './connectConfig.less';
import { UserConnect } from './userConnect';
import { AdvancedConfig } from './AdvancedConfig';

interface ConnectConfigProps {
  form: FormInstance;
}
export const ConnectConfig = memo((props: ConnectConfigProps) => {
  const { form } = props;
  const [isModalOpen, setIsModalOpen] = useState(false);
  const datasourceType = Form.useWatch('datasourceType', form);
  const host = Form.useWatch(['datasourceConfig', 'host'], form);
  const port = Form.useWatch(['datasourceConfig', 'port'], form);
  const athenaUrl = Form.useWatch(['datasourceConfig', 'athenaUrl'], form);

  const templateList = useMemo(() => {
    if (datasourceType === DataSourceType.HIVE) {
      return hiveConnections.find(item => item.connection === 'userConnection')?.templateList;
    }
    return otherConnections.find(item => item.connection === 'userConnection')?.templateList;
  }, [datasourceType]);

  useUpdateEffect(() => {
    const userConnectionListValues = form.getFieldValue(['users', 'datasourceConnection', 'userConnectionList']);
    const newUserConnectionListValues = userConnectionListValues.map(item => {
      if (item.connectionType === HiveTempType.ATHENA) {
        return {
          ...item,
          athenaUrl

        };
      }
      return {
        ...item,
        host,
        port
      };

    });
    form.setFieldValue(['users', 'datasourceConnection', 'userConnectionList'], newUserConnectionListValues);
  }, [host, port, athenaUrl]);
  return (
    <Form
      form={form}
      preserve={false}
      className={styles.container}
      labelCol={{ span: 7 }}
    >
      <div className={styles.header}>
        <div className={styles.title}>连接配置</div>
        <div className={styles.buttonGroup}>
          <Space size='small'>
            <Button type="link" onClick={() => setIsModalOpen(true)}>高级配置</Button>
          </Space>
        </div>
      </div>
      <Form.List
        name={['users', 'datasourceConnection', 'userConnectionList']}
        initialValue={[{ connectionType: HiveTempType.HIVE_SERVER, host, port, athenaUrl }]}
      >
        {(fields, { add, remove }) => (
          <>
            {fields.map(({ key, name, ...restField }) => {
              return (
                <div className={styles.connect} key={key}>
                  <UserConnect
                    form={form}
                    name={name}
                    restField={restField}
                    templateList={templateList}
                    remove={remove}
                    fieldsLength={fields.length}
                  />
                </div>);
            })}
            <div className={styles.footer}>
              <Button
                type="link"
                onClick={() => add({ connectionType: HiveTempType.HIVE_SERVER, host, port, athenaUrl })}
                shape="circle"
                disabled={fields.length >= 5}
                icon={<PlusCircleOutlined />} >
                添加连接
              </Button>
              <div className={styles.remark}>
                最多添加5个
              </div>
            </div>
          </>
        )}
      </Form.List>
      <AdvancedConfig
        form={form}
        isModalOpen={isModalOpen}
        setIsModalOpen={setIsModalOpen}
      />
    </Form>
  );
});
