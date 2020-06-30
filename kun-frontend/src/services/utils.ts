import { history, formatMessage } from 'umi';
import { message } from 'antd';
import axios, { AxiosResponse } from 'axios';
import qs from 'qs';

const axiosInstance = axios.create({
  baseURL: '/kun/api/v1',
});

export interface CommonResponse<T> {
  code: string;
  note: string;
  result: T;
}

async function commonHttp<T>(
  method: string,
  url: string,
  params?: object,
  options: object = {},
): Promise<T | null> {
  let accessBody;
  switch (method) {
    case 'get':
      accessBody = {
        method: 'get',
        url,
        params,
        paramsSerializer: function sfunc(params1: any) {
          return qs.stringify(params1, { indices: false });
        },
        ...options,
      };
      break;
    case 'post':
    case 'put':
    case 'patch':
    case 'delete':
      accessBody = {
        method,
        url,
        data: params,
        ...options,
      };
      break;

    default:
      accessBody = {
        method,
        url,
        data: params,
        ...options,
      };
      break;
  }

  try {
    const resp: AxiosResponse<CommonResponse<T>> = await axiosInstance(
      accessBody as any,
    );

    if (resp.status !== 200) {
      if (resp.status === 401) {
        history.push('/login');
        return null;
      }

      message.error(
        formatMessage({
          id: `common.webMessage.${resp.status}`,
          defaultMessage: formatMessage({ id: 'common.webMessage.unknown' }),
        }),
      );

      return null;
    }

    const { data } = resp;

    if (`${data.code}` !== '0') {
      message.error(data.note);
      return null;
    }

    return data.result;
  } catch (error) {
    if (error.response && error.response.status) {
      if (error.response.status !== 200) {
        if (error.response.status === 401) {
          history.push('/login');
          return null;
        }
        message.error(
          formatMessage({
            id: `common.webMessage.${error.response.status}`,
            defaultMessage: formatMessage({ id: 'common.webMessage.unknown' }),
          }),
        );
        return null;
      }
    }
    // eslint-disable-next-line
    console.log('error: ', error);

    return null;
  }
}

export async function get<T>(
  url: string,
  params?: object,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>('get', url, params, options);
  return result;
}

export async function post<T>(
  url: string,
  params?: object,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>('post', url, params, options);
  return result;
}

export async function put<T>(
  url: string,
  params?: object,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>('put', url, params, options);
  return result;
}

export async function patch<T>(
  url: string,
  params?: object,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>('patch', url, params, options);
  return result;
}

export async function deleteFunc<T>(
  url: string,
  params?: object,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>('delete', url, params, options);
  return result;
}
