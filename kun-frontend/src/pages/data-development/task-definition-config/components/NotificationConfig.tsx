import React, { memo, useCallback, useEffect, useState, useMemo } from 'react';
import { FormInstance } from 'antd/es/form';
import { NotifyWhen, UserNotifyConfigItem } from '@/definitions/NotifyConfig.type';
import { Checkbox, Col, Form, Radio, Row, Select } from 'antd';
import useI18n from '@/hooks/useI18n';
import styles from '@/pages/data-development/task-definition-config/components/BodyForm.less';
import LogUtils from '@/utils/logUtils';
import {
  useUpdateSelectEmailNotifierEffect,
  useUpdateSelectWeComNotifierEffect,
} from '@/pages/data-development/task-definition-config/components/NotificationConfig.helpers';
import { EmailExtraUserConfig } from '@/pages/data-development/task-definition-config/components/EmailExtraUserConfig';
import { useUpdateEffect } from 'ahooks';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

interface OwnProps {
  form: FormInstance;
  initNotificationWhen?: NotifyWhen | null;
  initUserNotificationConfigItems?: UserNotifyConfigItem[] | null;
  initTaskDefinition?: TaskDefinition;
}

type Props = OwnProps;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const logger = LogUtils.getLoggers('NotificationConfig');
const { Option } = Select;
const levelList = [1, 2, 3, 4, 5];
const hourList = Array(12)
  .fill(0)
  .map((i, idx) => idx);
const minuteList = Array(60)
  .fill(0)
  .map((i, idx) => idx);

export const NotificationConfig: React.FC<Props> = memo(function NotificationConfig(props) {
  const { form, initTaskDefinition, initNotificationWhen = null, initUserNotificationConfigItems = null } = props;

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

  const [openSla, setOpenSla] = useState(false);
  useEffect(() => {
    if (!initTaskDefinition?.taskPayload?.scheduleConfig.slaConfig) {
      setOpenSla(false);
    } else {
      setOpenSla(true);
    }
  }, [initTaskDefinition?.taskPayload?.scheduleConfig.slaConfig]);

  const noCheckOpenSla = useCallback(
    e => {
      setOpenSla(e.target.checked);

      if (!e.target.checked) {
        form.setFields([
          {
            name: ['taskPayload', 'scheduleConfig', 'slaConfig'],
            value: null,
          },
        ]);
      }
    },
    [form],
  );

  const slaRow = useMemo(
    () => (
      <Row style={{ marginBottom: 24 }}>
        <Col span={6}>
          <label style={{ color: '#526079' }} htmlFor="sla-type-select">{`${t(
            'dataDevelopment.definition.slaConfig.open',
          )}：`}</label>
        </Col>
        <Col span={18} id="sla-type-select">
          <Checkbox checked={openSla} onChange={noCheckOpenSla}>
            {t('dataDevelopment.definition.slaConfig.open')}
          </Checkbox>
        </Col>
      </Row>
    ),
    [noCheckOpenSla, openSla, t],
  );

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

      {slaRow}

      {openSla && (
        <Row>
          <Form.Item label={t('dataDevelopment.definition.slaConfig')}>
            <Col span={6}>
              <Form.Item
                label={t('dataDevelopment.definition.slaConfig.level')}
                name={['taskPayload', 'scheduleConfig', 'slaConfig', 'level']}
                rules={[{ required: true }]}
                initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.slaConfig?.level}
              >
                <Select style={{ width: 100 }}>
                  {levelList.map(i => (
                    <Option key={i} value={i}>
                      {i}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item label={t('dataDevelopment.definition.slaConfig.time')}>
                <Form.Item
                  label={t('dataDevelopment.definition.slaConfig.hours')}
                  name={['taskPayload', 'scheduleConfig', 'slaConfig', 'hours']}
                  rules={[{ required: true }]}
                  initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.slaConfig?.hours}
                >
                  <Select showSearch style={{ width: 100 }}>
                    {hourList.map(i => (
                      <Option key={i} value={i}>
                        {i}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
                <Form.Item
                  label={t('dataDevelopment.definition.slaConfig.minutes')}
                  name={['taskPayload', 'scheduleConfig', 'slaConfig', 'minutes']}
                  rules={[{ required: true }]}
                  initialValue={initTaskDefinition?.taskPayload?.scheduleConfig?.slaConfig?.minutes}
                >
                  <Select showSearch style={{ width: 100 }}>
                    {minuteList.map(i => (
                      <Option key={i} value={i}>
                        {i}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Form.Item>
            </Col>
          </Form.Item>
        </Row>
      )}
    </div>
  );
});
