// import { notification } from 'antd';
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

export interface whoamiServiceRespBody {
  username: string;
  permissions: string[];
}

export async function whoamiService() {
  const resp = await get<whoamiServiceRespBody>('/user/whoami');
  return resp;
}

export async function logoutService() {
  const resp = await post<{}>('/user/logout', {}, {});
  return resp;
}
