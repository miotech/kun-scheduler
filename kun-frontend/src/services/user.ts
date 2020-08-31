// import { notification } from 'antd';
import { User } from '@/definitions/User.type';
import { ServiceRespPromise } from '@/definitions/common-types';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { get, post } from '@/utils/requestUtils';

export interface LoginServiceReqBody {
  username: string;
  password: string;
}

export async function loginService(reqBody: LoginServiceReqBody) {
  try {
    const resp = await post('/user/login', {
      data: reqBody,
      prefix: DEFAULT_API_PREFIX,
    });
    return resp;
  } catch (e) {
    // eslint-disable-next-line
    console.log('e: ', e);
  }
  return null;
}

export interface WhoamiServiceRespBody {
  username: string;
  permissions: string[];
}

export async function whoamiService() {
  const resp = await get<WhoamiServiceRespBody>('/user/whoami');
  return resp;
}

export async function logoutService() {
  const resp = await post<{}>('/user/logout', {
    prefix: DEFAULT_API_PREFIX,
  });
  return resp;
}

export async function searchUsers(keyword: string = '') {
  return get('/user/search', {
    query: { keyword },
    prefix: DEFAULT_API_PREFIX,
  });
}

export async function fetchUsersList(): ServiceRespPromise<User[]> {
  return get('/user/list', {
    prefix: DEFAULT_API_PREFIX,
    mockCode: 'users.list',
  });
}
