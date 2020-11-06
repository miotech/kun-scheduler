import { KeyValuePair } from '@/components/KeyValueTable/KeyValueTable';
import forEach from 'lodash/forEach';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import cloneDeep from 'lodash/cloneDeep';
import isNil from 'lodash/isNil';
import isArray from 'lodash/isArray';

export function recordToKeyValuePairs(records: Record<string, string>): KeyValuePair[] {
  let i = 0;
  const pairs: KeyValuePair[] = [];
  forEach(records, (v, k) => {
    pairs.push({
      ordinal: i,
      key: k,
      value: v,
    });
    i += 1;
  });
  return pairs;
}

export function keyValuePairsToRecord(pairs: KeyValuePair[]): Record<string, string> {
  const record: Record<string, string> = {};
  pairs.forEach(pair => {
    record[pair.key] = pair.value;
  });
  return record;
}

export function normalizeTaskDefinition(taskDefinition: TaskDefinition, taskTemplate: TaskTemplate | null): TaskDefinition {
  if (!taskTemplate) {
    return taskDefinition;
  }
  const ret = cloneDeep(taskDefinition);
  forEach(taskTemplate.displayParameters || [], (parameter) => {
    if ((parameter.type === 'keyvalue') && !isNil(taskDefinition.taskPayload?.taskConfig?.[parameter.name])) {
      ret.taskPayload = ret.taskPayload ? {
        ...ret.taskPayload,
        taskConfig: {
          ...(ret.taskPayload?.taskConfig || {}),
          [parameter.name]: recordToKeyValuePairs(taskDefinition.taskPayload?.taskConfig?.[parameter.name]),
        },
      } : null;
    }
  });
  return ret;
}

export function transformFormTaskConfig(taskConfig: Record<string, any>, taskTemplate: TaskTemplate | null): Record<string, any> {
  if (!taskTemplate) {
    return taskConfig;
  }
  const ret = cloneDeep(taskConfig);
  forEach(taskTemplate.displayParameters || [], (parameter) => {
    if ((parameter.type === 'keyvalue') && isArray(taskConfig?.[parameter.name])) {
      ret[parameter.name] = keyValuePairsToRecord(taskConfig?.[parameter.name]);
    }
  });
  return ret;
}
