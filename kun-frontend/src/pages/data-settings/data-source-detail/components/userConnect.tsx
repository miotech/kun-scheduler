import React, { memo, useMemo, useState, useCallback, useEffect } from 'react';
import { Button, Form, Input, Select, Row, Col, Space, Radio } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { FormInstance } from 'antd/es/form';
import { DataSourceType, HiveTempType, hiveConnectionTemplateList } from '@/definitions/DataSource.type';
import useI18n from '@/hooks/useI18n';
import { useUpdateEffect } from 'ahooks';
import { UserSelect } from '@/components/UserSelect';
import { RootState } from '@/rematch/store';
import useRedux from '@/hooks/useRedux';
import { UserState } from '@/rematch/models/user';
import styles from './connectConfig.less';

interface UserConnectProps {
  form: FormInstance;
  templateList: HiveTempType[] | undefined;
  restField: any;
  name: number;
  fieldsLength: number;
  remove: (name: number) => void;
}
export const UserConnect = memo((props: UserConnectProps) => {
  const t = useI18n();
  const { form, templateList, name, remove, restField, fieldsLength } = props;
  const [authorizeType, setAuthorizeType] = useState('all');
  const datasourceType = Form.useWatch('datasourceType', form);
  const connectionType = Form.useWatch(['users', 'datasourceConnection', 'userConnectionList', name, 'connectionType'], form);
  const { selector } = useRedux<UserState>((state: RootState) => (state.user));
  const fields = useMemo(() => {
    return hiveConnectionTemplateList?.find(item => item.tempType === connectionType)?.fieldList;
  }, [connectionType]);

  useUpdateEffect(() => {
    form.setFieldValue(['users', 'datasourceConnection', 'userConnectionList', name, 'connectionType'], HiveTempType.HIVE_SERVER);
  }, [datasourceType]);

  const changeAuthorizeSelection = useCallback((value: string) => {
    if (value === 'all') {
      form.setFieldValue(['users', 'datasourceConnection', 'userConnectionList', name, 'securityUserList'], []);
    } else {
      form.setFieldValue(['users', 'datasourceConnection', 'userConnectionList', name, 'securityUserList'], [selector.username]);
    }
    setAuthorizeType(value);
  }, [form, name, selector]);

  return (
    <>
      <Row gutter={[8, 8]}>
        <Col span={6}>
          <Form.Item
            name={[name, 'name']}
            {...restField} label="连接1命名"
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>
        </Col>
        <Col span={6}>
          {templateList && templateList?.length > 1 &&
            <Form.Item
              {...restField}
              name={[name, 'connectionType']}
              label="连接方式"
              rules={[{ required: true }]}
            >
              <Radio.Group>
                {templateList.map(template => (
                  <Radio
                    key={template}
                    value={template}>
                    {t(`dataSettings.addUpdate.connectionTypeOption.${template}`)}
                  </Radio>
                ))}
              </Radio.Group>
            </Form.Item>
          }
        </Col>
        <Col span={6} offset={6}>
          {fieldsLength > 1 &&
            <DeleteOutlined
              className={styles.deleteOutlined}
              onClick={() => remove(name)}
            />
          }
        </Col>
      </Row>
      <Row gutter={[8, 8]}>
        {fields?.map(field => (
          <Col span={6}>
            <Form.Item
              name={[name, field.field]}
              label={t(`dataSettings.field.${field.field}`)}
              rules={[{ required: true }]}
            >
              <Input disabled={field.disabled}
              />
            </Form.Item>
          </Col>
        ))}

      </Row>
      <Row gutter={[8, 8]}>
        <Col span={6}>
          <Form.Item
            label='授权使用'
          >
            <Radio.Group
              value={authorizeType}
              onChange={(e) => changeAuthorizeSelection(e.target.value)}
            >
              <Radio value='all'>所有人</Radio>
              <Radio value='myself'>仅本人</Radio>
              <Radio value='others'>指定其他人</Radio>
            </Radio.Group>
          </Form.Item>
        </Col>
        <Col span={5}>
          <Form.Item
            name={[name, 'securityUserList']}
            rules={[{ required: true }]}
          >
            {authorizeType === 'others' &&
              <UserSelect
                mode='multiple'
              />
            }
          </Form.Item>
        </Col>
      </Row>
    </>);
});
