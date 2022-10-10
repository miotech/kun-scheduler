import React, { memo, useMemo, useState, useCallback, useEffect } from 'react';
import { Form, Input, Row, Col, Radio } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { FormInstance } from 'antd/es/form';
import { DataSourceInfo, DataSourceType, HiveTempType, FieldListMap } from '@/definitions/DataSource.type';
import useI18n from '@/hooks/useI18n';
import { useUpdateEffect } from 'ahooks';
import { UserSelect } from '@/components/UserSelect';
import { RootState } from '@/rematch/store';
import useRedux from '@/hooks/useRedux';
import { UserState } from '@/rematch/models/user';
import { dateFormatter } from '@/utils/datetime-utils';
import styles from './connectConfig.less';

interface UserConnectProps {
  form: FormInstance;
  userConnectionRadioList: HiveTempType[] | undefined;
  restField: any;
  name: number;
  fieldsLength: number;
  remove: (name: number) => void;
  isEditing: boolean;
  isCreate: boolean;
  dataSourceInfo: DataSourceInfo;
}
export const UserConnect = memo((props: UserConnectProps) => {
  const t = useI18n();
  const {
    form,
    userConnectionRadioList,
    name,
    remove,
    restField,
    fieldsLength,
    isEditing,
    isCreate,
    dataSourceInfo,
  } = props;
  const [authorizeType, setAuthorizeType] = useState('all');
  const datasourceType = Form.useWatch('datasourceType', form);
  const connectionType = Form.useWatch(
    ['datasourceConnection', 'userConnectionList', name, 'connectionConfigInfo', 'connectionType'],
    form,
  );

  const { selector } = useRedux<UserState>((state: RootState) => state.user);
  const fields = useMemo(() => {
    return FieldListMap[connectionType as HiveTempType];
  }, [connectionType]);

  useUpdateEffect(() => {
    const UserConnectConnectionType = form.getFieldValue([
      'datasourceConnection',
      'userConnectionList',
      name,
      'connectionConfigInfo',
      'connectionType',
    ]);
    if (datasourceType === DataSourceType.HIVE) {
      if (UserConnectConnectionType !== HiveTempType.ATHENA && UserConnectConnectionType !== HiveTempType.HIVE_SERVER) {
        form.setFieldValue(
          ['datasourceConnection', 'userConnectionList', name, 'connectionConfigInfo', 'connectionType'],
          HiveTempType.HIVE_SERVER,
        );
      }
    } else {
      form.setFieldValue(
        ['datasourceConnection', 'userConnectionList', name, 'connectionConfigInfo', 'connectionType'],
        datasourceType,
      );
    }
  }, [datasourceType]);

  const changeAuthorizeSelection = useCallback(
    (value: string) => {
      if (value === 'all') {
        form.setFieldValue(['datasourceConnection', 'userConnectionList', name, 'securityUserList'], []);
      } else {
        form.setFieldValue(
          ['datasourceConnection', 'userConnectionList', name, 'securityUserList'],
          [selector.username],
        );
      }
      setAuthorizeType(value);
    },
    [form, name, selector],
  );

  const currentUserConnection = useMemo(() => {
    return dataSourceInfo?.datasourceConnection?.userConnectionList[name];
  }, [dataSourceInfo?.datasourceConnection?.userConnectionList, name]);

  useEffect(() => {
    if (currentUserConnection) {
      if (currentUserConnection?.securityUserList.length === 0) {
        setAuthorizeType('all');
      } else {
        setAuthorizeType('others');
      }
    }
  }, [currentUserConnection, selector.username]);

  const isCreateUserConnection = useMemo(() => {
    return isCreate || (!currentUserConnection && isEditing);
  }, [isCreate, currentUserConnection, isEditing]);

  const isEdit = useMemo(() => {
    return (
      (currentUserConnection && isEditing && currentUserConnection?.createUser === selector?.username) ||
      isCreateUserConnection
    );
  }, [isEditing, currentUserConnection, selector?.username, isCreateUserConnection]);

  const deleteUserConnection = useCallback(() => {
    if (currentUserConnection) {
      dataSourceInfo?.datasourceConnection?.userConnectionList.splice(name, 1);
    }
    remove(name);
  }, [currentUserConnection, dataSourceInfo?.datasourceConnection?.userConnectionList, name, remove]);
  return (
    <>
      <Row gutter={[8, 8]}>
        <Col span={6}>
          {isEdit && (
            <Form.Item
              name={[name, 'name']}
              {...restField}
              label={t('dataSettings.connectConfig.name', { index: name + 1 })}
              rules={[{ required: true }]}
            >
              <Input autoComplete="off" />
            </Form.Item>
          )}
          {!isEdit && (
            <Form.Item
              name={[name, 'name']}
              {...restField}
              label={t('dataSettings.connectConfig.name', { index: name + 1 })}
              rules={[{ required: true }]}
            >
              <span className={styles.value}>{currentUserConnection?.name}</span>
            </Form.Item>
          )}
        </Col>
        <Col span={6}>
          {isEdit && (
            <Form.Item
              {...restField}
              name={[name, 'connectionConfigInfo', 'connectionType']}
              label={t('dataSettings.connectConfig.type')}
              rules={[{ required: true }]}
              hidden={userConnectionRadioList?.length === 1}
            >
              <Radio.Group>
                {userConnectionRadioList?.map(radio => (
                  <Radio key={radio} value={radio}>
                    {t(`dataSettings.addUpdate.connectionTypeOption.${radio}`)}
                  </Radio>
                ))}
              </Radio.Group>
            </Form.Item>
          )}
          {!isEdit && (
            <Form.Item
              name={[name, 'connectionConfigInfo', 'connectionType']}
              label={t('dataSettings.connectConfig.type')}
              hidden={userConnectionRadioList?.length === 1}
              rules={[{ required: true }]}
            >
              <span className={styles.value}>
                {t(
                  `dataSettings.addUpdate.connectionTypeOption.${currentUserConnection?.connectionConfigInfo?.connectionType}`,
                )}
              </span>
            </Form.Item>
          )}
        </Col>
        <Col span={6} offset={6}>
          {fieldsLength > 1 && isEdit && (
            <DeleteOutlined className={styles.deleteOutlined} onClick={() => deleteUserConnection()} />
          )}
        </Col>
      </Row>
      <Row gutter={[8, 8]}>
        {fields?.map(field => (
          <Col span={6}>
            {isEdit && (
              <Form.Item
                name={[name, 'connectionConfigInfo', field.field]}
                label={t(`dataSettings.field.${field.field}`)}
                rules={[{ required: field.required }]}
              >
                <Input disabled={field.disabled} autoComplete="off" />
              </Form.Item>
            )}
            {!isEdit && (
              <Form.Item
                name={[name, 'connectionConfigInfo', field.field]}
                label={t(`dataSettings.field.${field.field}`)}
                rules={[{ required: field.required }]}
              >
                <span className={styles.value}>
                  {field.encryption ? '***' : currentUserConnection?.connectionConfigInfo?.[field.field]}
                </span>
              </Form.Item>
            )}
          </Col>
        ))}
      </Row>
      <Row gutter={[8, 8]}>
        <Col span={6}>
          <Form.Item label={t('dataSettings.connectConfig.auth')}>
            <Radio.Group
              className={styles.value}
              value={authorizeType}
              disabled={!isEdit}
              onChange={e => changeAuthorizeSelection(e.target.value)}
            >
              <Radio value="all">{t('dataSettings.connectConfig.auth.all')}</Radio>
              <Radio value="others">{t('dataSettings.connectConfig.auth.others')}</Radio>
            </Radio.Group>
          </Form.Item>
        </Col>
        <Col span={5}>
          <Form.Item name={[name, 'securityUserList']} initialValue={[]}>
            {authorizeType === 'others' && (
              <UserSelect userDisabled={selector.username} disabled={!isEdit} mode="multiple" />
            )}
          </Form.Item>
        </Col>
      </Row>
      {currentUserConnection && (
        <Row gutter={[8, 8]}>
          <Col span={6}>
            <Form.Item label={t('dataSettings.addUpdate.createUser')}>
              <span className={styles.value}>{currentUserConnection?.createUser}</span>
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item label={t('dataSettings.addUpdate.createTime')}>
              <span className={styles.value}>{dateFormatter(currentUserConnection?.createTime)}</span>
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item label={t('dataSettings.addUpdate.updateUser')}>
              <span className={styles.value}>{currentUserConnection?.updateUser}</span>
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item label={t('dataSettings.addUpdate.updateTime')}>
              <span className={styles.value}>{dateFormatter(currentUserConnection?.updateTime)}</span>
            </Form.Item>
          </Col>
        </Row>
      )}
    </>
  );
});
