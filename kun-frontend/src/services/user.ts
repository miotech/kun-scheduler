// import { notification } from 'antd';
import { User } from '@/definitions/User.type';
import { ServiceRespPromise } from '@/definitions/common-types';
import { SECURITY_API_PRIFIX } from '@/constants/api-prefixes';
import { get, post } from '@/utils/requestUtils';
import LogUtils from '@/utils/logUtils';

export interface Oauth2CallbackResponse {
  url: string;
  clientId: string;
  responseType: string;
  redirectUri: string;
}
export interface LoginServiceReqBody {
  username: string;
  password: string;
}

export async function loginService(reqBody: LoginServiceReqBody) {
  const logger = LogUtils.getLoggers('loginService');
  try {
    const resp = await post('/login', {
      data: reqBody,
      prefix: SECURITY_API_PRIFIX,
    });
    return resp;
  } catch (e) {
    logger.error(e);
  }
  return null;
}

export interface WhoamiServiceRespBody {
  username: string;
  permissions: string[];
}

export async function whoamiService() {
  const resp = await get<WhoamiServiceRespBody>('/whoami', {
    prefix: SECURITY_API_PRIFIX,
  });
  return resp;
}

export async function logoutService() {
  const resp = await post<{}>('/logout', {
    prefix: SECURITY_API_PRIFIX,
  });
  return resp;
}

export async function searchUsers(keyword: string = '') {
  return get('/user/search', {
    query: { keyword },
    prefix: SECURITY_API_PRIFIX,
  });
}

export async function fetchUsersList(): ServiceRespPromise<User[]> {
  return get('/user/list', {
    prefix: SECURITY_API_PRIFIX,
    mockCode: 'users.list',
  });
}

export async function OAuthLogin(params) {
  return get('/oauth2/authorize', {
    query: params,
    prefix: SECURITY_API_PRIFIX,
  });
}

export async function getOAuthUrl() {
  return get<Oauth2CallbackResponse>('/oauth/callback-url', {
    prefix: SECURITY_API_PRIFIX,
  });
}
