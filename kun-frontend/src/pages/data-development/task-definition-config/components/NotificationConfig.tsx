import React, { memo, useCallback, useEffect, useState } from 'react';
import { FormInstance } from 'antd/es/form';
import { NotifyWhen, UserNotifyConfigItem } from '@/definitions/NotifyConfig.type';
import { Checkbox, Col, Form, Radio, Row } from 'antd';
import useI18n from '@/hooks/useI18n';
import styles from '@/pages/data-development/task-definition-config/components/BodyForm.less';
import LogUtils from '@/utils/logUtils';
import {
  useUpdateSelectEmailNotifierEffect,
  useUpdateSelectWeComNotifierEffect,
} from '@/pages/data-development/task-definition-config/components/NotificationConfig.helpers';
import { EmailExtraUserConfig } from '@/pages/data-development/task-definition-config/components/EmailExtraUserConfig';
import { useUpdateEffect } from 'ahooks';

interface OwnProps {
  form: FormInstance;
  initNotificationWhen?: NotifyWhen | null;
  initUserNotificationConfigItems?: UserNotifyConfigItem[] | null;
}

type Props = OwnProps;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const logger = LogUtils.getLoggers('NotificationConfig');

export const NotificationConfig: React.FC<Props> = memo(function NotificationConfig(props) {
  const { form, initNotificationWhen = null, initUserNotificationConfigItems = null } = props;

  const t = useI18n();

  const [notifyWhenState, setNotifyWhenState] = useState<NotifyWhen>(NotifyWhen.SYSTEM_DEFAULT);
  const [weComNotifierEnabled, setWeComNotifierEnabled] = useState<boolean>(
    (initUserNotificationConfigItems || []).find(configItem => configItem.notifierType === 'WECOM') != null,
  );
  const [emailNotifierEnabled, setEmailNotifierEnabled] = useState<boolean>(
    (initUserNotificationConfigItems || []).find(configItem => configItem.notifierType === 'EMAIL') != null,
  );

  const [emailConfigItemIndex, setEmailConfigItemIndex] = useState<number>(
    (initUserNotificationConfigItems || []).findIndex(configItem => configItem.notifierType === 'EMAIL'),
  );

  const updateNotifierConfigItemsIndices = useCallback(function updateNotifierConfigItemsIndices(
    updatedConfigItems: UserNotifyConfigItem[],
  ) {
    setEmailConfigItemIndex(updatedConfigItems.findIndex(configItem => configItem.notifierType === 'EMAIL'));
  },
  []);

  useEffect(() => {
    if (initNotificationWhen != null) {
      setNotifyWhenState(initNotificationWhen);
      form.setFieldsValue({
        taskPayload: {
          notifyConfig: {
            notifyWhen: initNotificationWhen,
          },
        },
      });
    }
    if (initUserNotificationConfigItems != null) {
      form.setFieldsValue({
        taskPayload: {
          notifyConfig: {
            notifierConfig: initUserNotificationConfigItems,
          },
        },
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initNotificationWhen, initUserNotificationConfigItems]);

  useUpdateEffect(() => {
    if (notifyWhenState === NotifyWhen.NEVER || notifyWhenState === NotifyWhen.SYSTEM_DEFAULT) {
      setWeComNotifierEnabled(false);
      setEmailNotifierEnabled(false);
    }
  }, [notifyWhenState]);

  // Apply helper hooks
  useUpdateSelectEmailNotifierEffect(emailNotifierEnabled, form, updateNotifierConfigItemsIndices);
  useUpdateSelectWeComNotifierEffect(weComNotifierEnabled, form, updateNotifierConfigItemsIndices);

  function renderSelectNotifierTypes(notifyWhen: NotifyWhen) {
    if (notifyWhen === NotifyWhen.SYSTEM_DEFAULT || notifyWhen === NotifyWhen.NEVER) {
      return <React.Fragment />;
    }
    // else
    return (
      <Row>
        <Col span={6}>
          <label style={{ color: '#526079' }} htmlFor="notifier-type-select">{`${t(
            'dataDevelopment.definition.notificationConfig.notifers',
          )}：`}</label>
        </Col>
        <Col span={18} id="notifier-type-select">
          {/* 屏蔽企业微信 */}
          {/* <Checkbox checked={weComNotifierEnabled} onChange={e => setWeComNotifierEnabled(e.target.checked)}>
            {t('dataDevelopment.definition.notificationConfig.WECOM')}
          </Checkbox> */}
          <Checkbox checked={emailNotifierEnabled} onChange={e => setEmailNotifierEnabled(e.target.checked)}>
            {t('dataDevelopment.definition.notificationConfig.EMAIL')}
          </Checkbox>
        </Col>
      </Row>
    );
  }

  // noinspection RequiredAttributes
  return (
    <div id="notification-config-form-container" className={styles.NotificationConfig}>
      {/* Trigger type / Notify when */}
      <Form.Item
        label={t('dataDevelopment.definition.notificationConfig.notifyWhen')}
        name={['taskPayload', 'notifyConfig', 'notifyWhen']}
        rules={[{ required: true }]}
        initialValue={initNotificationWhen == null ? NotifyWhen.SYSTEM_DEFAULT : initNotificationWhen}
      >
        <Radio.Group
          onChange={ev => {
            setNotifyWhenState(ev.target.value);
          }}
        >
          <Radio value={NotifyWhen.SYSTEM_DEFAULT}>
            {t('dataDevelopment.definition.notificationConfig.notifyWhen.systemDefault')}
          </Radio>
          <Radio value={NotifyWhen.ON_FAIL}>
            {t('dataDevelopment.definition.notificationConfig.notifyWhen.onFail')}
          </Radio>
          <Radio value={NotifyWhen.ON_SUCCESS}>
            {t('dataDevelopment.definition.notificationConfig.notifyWhen.onSuccess')}
          </Radio>
          <Radio value={NotifyWhen.ON_FINISH}>
            {t('dataDevelopment.definition.notificationConfig.notifyWhen.onFinish')}
          </Radio>
          <Radio value={NotifyWhen.NEVER}>{t('dataDevelopment.definition.notificationConfig.notifyWhen.never')}</Radio>
        </Radio.Group>
      </Form.Item>
      {/* Notifier user custom configs */}
      {renderSelectNotifierTypes(notifyWhenState)}
      {/* Custom configs */}
      <EmailExtraUserConfig form={form} emailUserConfigIndex={emailConfigItemIndex} />
    </div>
  );
});
