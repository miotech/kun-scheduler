import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';
import { delet, get, post, put } from '@/utils/requestUtils';

import { ServiceRespPromise } from '@/definitions/common-types';
import {
  GlobalVariable,
  GlobalVariableUpsertVO,
} from '@/definitions/GlobalVariable.type';

export async function fetchGlobalVariablesList(): ServiceRespPromise<
  GlobalVariable[]
> {
  return get('/variables', {
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}

export async function createGlobalVariable(
  createVO: GlobalVariableUpsertVO,
): ServiceRespPromise<GlobalVariable> {
  return post('/variables', {
    prefix: API_DATA_PLATFORM_PREFIX,
    data: {
      key: createVO.key,
      value: createVO.value,
      encrypted: createVO.encrypted,
    },
  });
}

export async function updateGlobalVariable(
  createVO: GlobalVariableUpsertVO,
): ServiceRespPromise<GlobalVariable> {
  return put('/variables', {
    prefix: API_DATA_PLATFORM_PREFIX,
    data: {
      key: createVO.key,
      value: createVO.value,
      encrypted: createVO.encrypted,
    },
  });
}

export async function deleteGlobalVariable(
  variableKey: string,
): ServiceRespPromise<boolean> {
  return delet('/variables/:variableKey', {
    pathParams: {
      variableKey,
    },
    prefix: API_DATA_PLATFORM_PREFIX,
  });
}
