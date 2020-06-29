import {post, CommonResponse} from './utils';

export interface LoginServiceReqBody {
  username: string;
  password: string;
}

export async function loginService(reqBody: LoginServiceReqBody) {
  const resp = await post('/user/login', reqBody);
  return resp;
}

export interface whoamiServiceRespBody extends CommonResponse {
  name: string;
}

export async function whoamiService() {
  const resp = await post<whoamiServiceRespBody>('/user/whoami');
  return resp;
}
