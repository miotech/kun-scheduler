import React, { memo, useMemo, useState } from 'react';
import { Button, Form, Space } from 'antd';
import { FormInstance } from 'antd/es/form';
import { PlusCircleOutlined } from '@ant-design/icons';
import { HiveTempType, DataSourceType, databaseTemplateMap, DataSourceInfo } from '@/definitions/DataSource.type';
import { useUpdateEffect } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import styles from './connectConfig.less';
import { UserConnect } from './userConnect';
import { AdvancedConfig } from './AdvancedConfig';

interface ConnectConfigProps {
  form: FormInstance;
  AdvancedConfigForm: FormInstance;
  advancedConfigFormData: Object;
  setAdvancedConfigFormData: (value: Object) => void;
  isEditing: boolean;
  isCreate: boolean;
  dataSourceInfo: DataSourceInfo;
}
export const ConnectConfig = memo((props: ConnectConfigProps) => {
  const t = useI18n();
  const {
    form,
    AdvancedConfigForm,
    isEditing,
    isCreate,
    dataSourceInfo,
    advancedConfigFormData,
    setAdvancedConfigFormData,
  } = props;
  const [isModalOpen, setIsModalOpen] = useState(false);
  const datasourceType = Form.useWatch('datasourceType', form);
  const host = Form.useWatch(['datasourceConfigInfo', 'host'], form);
  const port = Form.useWatch(['datasourceConfigInfo', 'port'], form);
  const athenaUrl = Form.useWatch(['datasourceConfigInfo', 'athenaUrl'], form);

  const userConnectionRadioList = useMemo(() => {
    return databaseTemplateMap[datasourceType as DataSourceType]?.find(item => item.connection === 'userConnection')
      ?.templateList;
  }, [datasourceType]);

  useUpdateEffect(() => {
    const userConnectionListValues = form.getFieldValue(['datasourceConnection', 'userConnectionList']);
    const newUserConnectionListValues = userConnectionListValues?.map(item => {
      if (item?.connectionConfigInfo?.connectionType === HiveTempType.ATHENA) {
        return {
          ...item,
          connectionConfigInfo: {
            ...item.connectionConfigInfo,
            athenaUrl,
          },
        };
      }
      return {
        ...item,
        connectionConfigInfo: {
          ...item.connectionConfigInfo,
          host,
          port,
        },
      };
    });
    form.setFieldValue(['datasourceConnection', 'userConnectionList'], newUserConnectionListValues);
  }, [host, port, athenaUrl]);

  const isEdit = useMemo(() => {
    return isEditing || isCreate;
  }, [isEditing, isCreate]);

  return (
    <Form form={form} className={styles.container} labelCol={{ span: 8 }}>
      <div className={styles.header}>
        <div className={styles.title}>{t('dataSettings.connectConfig')}</div>
        <div className={styles.buttonGroup}>
          <Space size="small">
            <Button type="link" disabled={!datasourceType} onClick={() => setIsModalOpen(true)}>
              {t('dataSettings.advancedConfig')}
            </Button>
          </Space>
        </div>
      </div>
      <Form.List name={['datasourceConnection', 'userConnectionList']}>
        {(fields, { add, remove }) => (
          <>
            {fields.map(({ key, name, ...restField }) => {
              return (
                <div className={styles.connect} key={key}>
                  <UserConnect
                    form={form}
                    name={name}
                    restField={restField}
                    isEditing={isEditing}
                    isCreate={isCreate}
                    dataSourceInfo={dataSourceInfo}
                    userConnectionRadioList={userConnectionRadioList}
                    remove={remove}
                    fieldsLength={fields.length}
                  />
                </div>
              );
            })}
            <div className={styles.footer}>
              <Button
                type="link"
                onClick={() => add({ connectionConfigInfo: { host, port, athenaUrl } })}
                shape="circle"
                disabled={fields.length >= 5 || !datasourceType || !isEdit}
                icon={<PlusCircleOutlined />}
              >
                {t('dataSettings.connectConfig.addButton')}
              </Button>
              <div className={styles.remark}>{t('dataSettings.connectConfig.addNotice')}</div>
            </div>
          </>
        )}
      </Form.List>
      {isModalOpen && (
        <AdvancedConfig
          form={form}
          isEdit={isEdit}
          advancedConfigFormData={advancedConfigFormData}
          AdvancedConfigForm={AdvancedConfigForm}
          isModalOpen={isModalOpen}
          dataSourceInfo={dataSourceInfo}
          setIsModalOpen={setIsModalOpen}
          setAdvancedConfigFormData={setAdvancedConfigFormData}
        />
      )}
    </Form>
  );
});
