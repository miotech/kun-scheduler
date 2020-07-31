import { get, post } from './utils';

export interface LoginServiceReqBody {
  username: string;
  password: string;
}

export async function loginService(reqBody: LoginServiceReqBody) {
  const resp = await post('/user/login', reqBody);
  return resp;
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
