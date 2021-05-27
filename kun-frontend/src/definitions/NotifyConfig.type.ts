export enum NotifyWhen {
  SYSTEM_DEFAULT = 'SYSTEM_DEFAULT',
  ON_FAIL = 'ON_FAIL',
  ON_SUCCESS = 'ON_SUCCESS',
  ON_FINISH = 'ON_FINISH',
  NEVER = 'NEVER',
}

export enum NotifierType {
  WECOM = 'WECOM',
  EMAIL = 'EMAIL',
}

export interface UserNotifyConfigItem {
  notifierType: NotifierType;
  [key: string]: any;
}

export interface WeComNotifyConfigItem extends UserNotifyConfigItem {
  notifierType: NotifierType.WECOM;
}

export interface EmailNotifyConfigItem extends UserNotifyConfigItem {
  notifierType: NotifierType.EMAIL;
  emailList: string[];
  userIdList: string[];
}
