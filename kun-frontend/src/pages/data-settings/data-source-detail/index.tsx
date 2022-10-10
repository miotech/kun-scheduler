import React, { useState, useEffect, useCallback } from 'react';
import { Form, Spin } from 'antd';
import useUrlState from '@ahooksjs/use-url-state';
import { getDatabaseService } from '@/services/dataSettings';
import { useRequest, useUpdateEffect } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import { BasicInformation } from './components/basicInformation';
import { ConnectConfig } from './components/connectConfig';
import styles from './index.less';

export default function DataSettingDetail() {
  const t = useI18n();
  const [form] = Form.useForm();
  const [AdvancedConfigForm] = Form.useForm();
  const [isEditing, setIsEditing] = useState(false);
  const [isCreate, setIsCreate] = useState(false);
  const [query] = useUrlState();

  const [advancedConfigFormData, setAdvancedConfigFormData] = useState({});

  const { data, loading, run: getDatabaseServiceRun } = useRequest(getDatabaseService, {
    debounceWait: 300,
    manual: true,
  });

  const queryDatabaseService = useCallback(() => {
    setIsCreate(false);
    getDatabaseServiceRun(query.id);
  }, [getDatabaseServiceRun, query.id]);

  useEffect(() => {
    if (query?.id) {
      queryDatabaseService();
    } else {
      setIsCreate(true);
    }
  }, [query, getDatabaseServiceRun, queryDatabaseService]);

  useUpdateEffect(() => {
    if (data) {
      form.setFieldsValue(data);
      setAdvancedConfigFormData(data?.datasourceConnection);
    }
  }, [data]);
  return (
    <div className={styles.container}>
      <Spin spinning={loading}>
        <BasicInformation
          form={form}
          advancedConfigFormData={advancedConfigFormData}
          isEditing={isEditing}
          isCreate={isCreate}
          setIsEditing={setIsEditing}
          dataSourceInfo={data}
          queryDatabaseService={queryDatabaseService}
        />
        <ConnectConfig
          form={form}
          AdvancedConfigForm={AdvancedConfigForm}
          isEditing={isEditing}
          isCreate={isCreate}
          dataSourceInfo={data}
          advancedConfigFormData={advancedConfigFormData}
          setAdvancedConfigFormData={setAdvancedConfigFormData}
        />
        <div className={styles.tips}>
          <div className={styles.title}>{t('dataSettings.connectConfig.notice')}</div>

          <div className={styles.content}>
            1.{t('dataSettings.connectConfig.notice.tip1')}；<br />
            2.{t('dataSettings.connectConfig.notice.tip2')};<br />
            3.{t('dataSettings.connectConfig.notice.tip3')};
          </div>
        </div>
      </Spin>
    </div>
  );
}
