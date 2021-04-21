import { FormInstance } from 'antd/es/form';
import {
  EmailNotifyConfigItem,
  NotifierType,
  UserNotifyConfigItem,
  WeComNotifyConfigItem,
} from '@/definitions/NotifyConfig.type';
import { useUpdateEffect } from 'ahooks';

export function useUpdateSelectEmailNotifierEffect(
  emailNotifierEnabled: boolean,
  form: FormInstance,
  updateNotifierConfigItemsIndices: any,
) {
  useUpdateEffect(
    function useUpdateSelectEmailNotifierEffectFn() {
      if (emailNotifierEnabled) {
        const userNotifyConfigItems: UserNotifyConfigItem[] =
          form.getFieldValue(['taskPayload', 'notifyConfig', 'notifierConfig']) || [];
        const existingEmailConfigItem = userNotifyConfigItems.find(item => item.notifierType === NotifierType.EMAIL) as
          | EmailNotifyConfigItem
          | undefined;
        if (!existingEmailConfigItem) {
          const nextNotifierConfigState = [
            ...userNotifyConfigItems,
            {
              notifierType: NotifierType.EMAIL,
              emailList: [],
              userIdList: [],
            } as EmailNotifyConfigItem,
          ];
          form.setFieldsValue({
            taskPayload: {
              notifyConfig: {
                notifierConfig: nextNotifierConfigState,
              },
            },
          });
          updateNotifierConfigItemsIndices(nextNotifierConfigState);
        }
      } else {
        const userNotifyConfigItems: UserNotifyConfigItem[] =
          form.getFieldValue(['taskPayload', 'notifyConfig', 'notifierConfig']) || [];
        const nextNotifierConfigState = userNotifyConfigItems.filter(item => item.notifierType !== NotifierType.EMAIL);
        form.setFieldsValue({
          taskPayload: {
            notifyConfig: {
              notifierConfig: nextNotifierConfigState,
            },
          },
        });
        updateNotifierConfigItemsIndices(nextNotifierConfigState);
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [emailNotifierEnabled],
  );
}

export function useUpdateSelectWeComNotifierEffect(
  weComNotifierEnabled: boolean,
  form: FormInstance,
  updateNotifierConfigItemsIndices: any,
) {
  useUpdateEffect(
    function useUpdateSelectWeComNotifierEffectFn() {
      if (weComNotifierEnabled) {
        const userNotifyConfigItems: UserNotifyConfigItem[] =
          form.getFieldValue(['taskPayload', 'notifyConfig', 'notifierConfig']) || [];
        const existingWeComConfigItem = userNotifyConfigItems.find(item => item.notifierType === NotifierType.WECOM) as
          | WeComNotifyConfigItem
          | undefined;
        if (!existingWeComConfigItem) {
          const nextNotifierConfigState = [
            ...userNotifyConfigItems,
            {
              notifierType: NotifierType.WECOM,
            } as WeComNotifyConfigItem,
          ];
          form.setFieldsValue({
            taskPayload: {
              notifyConfig: {
                notifierConfig: nextNotifierConfigState,
              },
            },
          });
          updateNotifierConfigItemsIndices(nextNotifierConfigState);
        }
      } else {
        const userNotifyConfigItems: UserNotifyConfigItem[] =
          form.getFieldValue(['taskPayload', 'notifyConfig', 'notifierConfig']) || [];
        const nextNotifierConfigState = userNotifyConfigItems.filter(item => item.notifierType !== NotifierType.WECOM);
        form.setFieldsValue({
          taskPayload: {
            notifyConfig: {
              notifierConfig: nextNotifierConfigState,
            },
          },
        });
        updateNotifierConfigItemsIndices(nextNotifierConfigState);
      }
    },
    [weComNotifierEnabled],
  );
}
