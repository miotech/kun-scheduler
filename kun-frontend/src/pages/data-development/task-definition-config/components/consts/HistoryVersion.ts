interface KeysMap {
  [key: string]: string;
}

interface ValuesMap {
  [key: string]: {
    [key: string]: string;
  };
}

export const valuesMap: ValuesMap = {
  notifyWhen: {
    SYSTEM_DEFAULT: 'dataDevelopment.definition.notificationConfig.notifyWhen.systemDefault',
    ON_FAIL: 'dataDevelopment.definition.notificationConfig.notifyWhen.onFail',
    ON_SUCCESS: 'dataDevelopment.definition.notificationConfig.notifyWhen.onSuccess',
    ON_FINISH: 'dataDevelopment.definition.notificationConfig.notifyWhen.onFinish',
    NEVER: 'dataDevelopment.definition.notificationConfig.notifyWhen.neve',
  },
  type: {
    ONESHOT: 'dataDevelopment.definition.scheduleConfig.scheduleType.oneShot',
    SCHEDULED: 'dataDevelopment.definition.scheduleConfig.scheduleType.scheduled',
    NONE: 'dataDevelopment.definition.scheduleConfig.scheduleType.manual',
  },
  blockType: {
    NONE: 'dataDevelopment.definition.scheduleConfig.blockConfig.NONE',
    WAIT_PREDECESSOR: 'dataDevelopment.definition.scheduleConfig.blockConfig.WAIT_PREDECESSOR',
    WAIT_PREDECESSOR_DOWNSTREAM: 'dataDevelopment.definition.scheduleConfig.blockConfig.WAIT_PREDECESSOR_DOWNSTREAM',
  },
};

export const keysMap: KeysMap = {
  taskConfig: 'dataDevelopment.definition.paramConfig',
  scheduleConfig: 'dataDevelopment.definition.scheduleConfig',
  notifyConfig: 'dataDevelopment.definition.notificationConfig',
  type: 'dataDevelopment.definition.scheduleConfig.scheduleType',
  cronExpr: 'dataDevelopment.definition.scheduleConfig.cronExpression',
  blockType: 'dataDevelopment.definition.scheduleConfig.blockType',
  inputDatasets: 'dataDevelopment.definition.scheduleConfig.upstream.type.inputDataset',
  outputDatasets: 'dataDevelopment.definition.scheduleConfig.output.outputDataset',
  retryDelay: 'dataDevelopment.definition.scheduleConfig.retryDelay',
  notifyWhen: 'dataDevelopment.definition.notificationConfig.notifyWhen',
  slaConfig: 'dataDevelopment.definition.slaConfig',
  retries: 'dataDevelopment.definition.scheduleConfig.retireTimes',
};

export function findKeys(jsonObj: any, keys: string[] = []) {
  Object.keys(jsonObj).forEach(key => {
    if (jsonObj[key] instanceof Object && !Array.isArray(jsonObj[key])) {
      keys.push(key);
      findKeys(jsonObj[key], keys);
    } else {
      keys.push(key);
    }
  });
  return keys;
}
