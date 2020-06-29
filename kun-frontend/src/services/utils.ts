import { history, formatMessage } from 'umi';
import { notification } from 'antd';
import axios, { AxiosResponse } from 'axios';
import qs from 'qs';
import { LogUtils } from '@/utils/logUtils';
import { ServiceRespPromise } from '@/definitions/common-types';

export const baseURL = '/kun/api/v1';

export const axiosInstance = axios.create({
  baseURL,
});

const errorLog = LogUtils.getErrorLogger('commonHttp');

export interface CommonResponse<T> {
  code: string;
  note: string;
  result: T;
}

async function commonHttp<T>(
  method: string,
  url: string,
  params?: object,
  defaultValue?: T | null,
  options: object = {},
): ServiceRespPromise<T> {
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
        if (url !== '/login') {
          history.push('/login');
        }
        return null;
      }

      notification.error({
        message: formatMessage({
          id: `common.webMessage.${resp.status}`,
          defaultMessage: formatMessage({ id: 'common.webMessage.unknown' }),
        }),
        description: resp?.data?.note ?? '',
      });

      return null;
    }

    const { data } = resp;

    if (`${data.code}` !== '0') {
      notification.error({
        message: data.note,
      });
      return null;
    }

    if (data.result) {
      return data.result;
    }
    return defaultValue || null;
  } catch (error) {
    if (error.response && error.response.status) {
      if (error.response.status !== 200) {
        if (error.response.status === 401) {
          if (url !== '/login') {
            history.push('/login');
          }
          return null;
        }
        notification.error({
          message: formatMessage({
            id: `common.webMessage.${error.response.status}`,
            defaultMessage: formatMessage({ id: 'common.webMessage.unknown' }),
          }),
          description: '',
        });
        return null;
      }
    }

    errorLog('[Request Error]: %o', error);

    return null;
  }
}

export async function get<T>(
  url: string,
  params?: object,
  defaultValue?: T | null,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>('get', url, params, defaultValue, options);
  return result;
}

export async function post<T>(
  url: string,
  params?: object,
  defaultValue?: T | null,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>(
    'post',
    url,
    params,
    defaultValue,
    options,
  );
  return result;
}

export async function put<T>(
  url: string,
  params?: object,
  defaultValue?: T | null,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>('put', url, params, defaultValue, options);
  return result;
}

export async function patch<T>(
  url: string,
  params?: object,
  defaultValue?: T | null,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>(
    'patch',
    url,
    params,
    defaultValue,
    options,
  );
  return result;
}

export async function deleteFunc<T>(
  url: string,
  params?: object,
  defaultValue?: T | null,
  options: object = {},
): Promise<T | null> {
  const result = await commonHttp<T>(
    'delete',
    url,
    params,
    defaultValue,
    options,
  );
  return result;
}
