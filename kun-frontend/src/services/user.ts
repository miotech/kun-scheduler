// import { notification } from 'antd';
import { User } from '@/definitions/User.type';
import { ServiceRespPromise } from '@/definitions/common-types';
import { get as serviceGet } from '@/utils/requestUtils';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { get, post, axiosInstance } from './utils';

export interface LoginServiceReqBody {
  username: string;
  password: string;
}

export async function loginService(reqBody: LoginServiceReqBody) {
  try {
    const resp = await axiosInstance({
      method: 'POST',
      data: reqBody,
      url: '/user/login',
    });
    return resp.data || {};
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
  const resp = await post<{}>('/user/logout', {}, {});
  return resp;
}

export async function searchUsers(keyword: string = '') {
  return get('/user/search', { keyword });
}

export async function fetchUsersList(): ServiceRespPromise<User[]> {
  return serviceGet('/user/list', {
    prefix: DEFAULT_API_PREFIX,
    mockCode: 'users.list',
  });
}
