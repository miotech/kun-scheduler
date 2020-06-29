import axios, { CancelToken, Method } from 'axios';
import { LogUtils } from '@/utils/logUtils';
import { history } from '@@/core/history';
import { notification } from 'antd';
import forIn from 'lodash/forIn';
import micromatch from 'micromatch';
import { formatMessage } from '@@/plugin-locale/localeExports';
import SafeUrlAssembler from 'safe-url-assembler';
import qs from 'qs';
import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';

declare const USE_MOCK_CONFIG: string | undefined;

declare const DISABLE_MOCK: boolean | undefined;

// @ts-ignore
window.MOCK_FUNC_CONFIG_GLOBAL = USE_MOCK_CONFIG;
const disableMock = process?.env?.DISABLE_MOCK || DISABLE_MOCK || false;

let mockCodeConfig = {};
if (typeof USE_MOCK_CONFIG === 'string') {
  mockCodeConfig = JSON.parse(USE_MOCK_CONFIG || '{}');
}

const mockCodePatterns: string[] = [];
forIn(mockCodeConfig, (enabled: any, pattern: string) => {
  if (enabled) {
    mockCodePatterns.push(pattern);
  }
});

const logger = LogUtils.getLoggers('RequestUtils');
const axiosInstance = axios.create();

export interface GetRequestOptions {
  headers?: any;
  pathParams?: Record<string, string | number>;
  query?: Record<string, any>;
  prefix?: string;
  withCredentials?: boolean;
  cancelToken?: CancelToken;
  xsrfCookieName?: string;
  xsrfHeaderName?: string;
  // a configurable mock code
  mockCode?: string;
  // enforce using mock
  useMock?: boolean;
}

export interface RequestOptions extends GetRequestOptions {
  data?: Record<any, any>;
}

export interface GenericRequestOptions extends RequestOptions {
  method?: Method,
}

// Add a response interceptor
axiosInstance.interceptors.response.use((response) => {
  // Any status code that lie within the range of 2xx cause this function to trigger
  // Do something with response data
  const { data } = response;
  if (`${data?.code}` !== '0') {
    notification.error({
      message: formatMessage({
        id: `common.webMessage.${response.status}`,
        defaultMessage: formatMessage({ id: 'common.webMessage.unknown' }),
      }),
      description: data?.note ?? '',
    });
    return Promise.reject(data);
  }
  // if success
  if (response.data?.result) {
    return response.data.result;
  }
  // if `result` property not exists, reject
  return Promise.resolve(null);
}, (error) => {
  if (error.response && error.response.status) {
    // if credential info required
    if (error.response.status === 401) {
      if (window.location.pathname !== '/login') {
        history.push('/login');
      }
    } else if (error.response.status >= 400) {
      notification.error({
        message: formatMessage({
          id: `common.webMessage.${error.response.status}`,
          defaultMessage: formatMessage({ id: 'common.webMessage.unknown' }),
        }),
        description: '',
      });
    }
  }
  logger.error('[Request Error]: %o', error);
  // Any status codes that falls outside the range of 2xx cause this function to trigger
  // Do something with response error
  return Promise.reject(error);
});

export function request<T>(urlTemplate: string, options: GenericRequestOptions = {}): Promise<T> {
  // 检查 mockCode 是否匹配 use-mock.config.yaml 中的 pattern，如果匹配则使用 mock API
  if (options.mockCode && mockCodePatterns && mockCodePatterns.length) {
    const shouldUseMock = micromatch.some(options.mockCode, mockCodePatterns);
    if (shouldUseMock) {
      options.prefix = options.prefix?.replace('/api', '/api-mock');
      delete options.mockCode;
      delete options.useMock;
    }
  }
  if (options && options.useMock && !disableMock) {
    options.prefix = options.prefix?.replace('/api', '/api-mock');
    delete options.useMock;
  }

  return axiosInstance.request({
    url: SafeUrlAssembler()
      .template(urlTemplate)
      .param(options.pathParams || {})
      .toString(),
    params: {
      ...options.query,
    },
    paramsSerializer: (params: any) => {
      return qs.stringify(params, { indices: false });
    },
    baseURL: options.prefix || DEFAULT_API_PREFIX,
    data: options.data,
    // by default, we send credentials
    withCredentials: (typeof options.withCredentials === 'boolean') ? options.withCredentials : true,
    method: options.method,
    headers: options.headers,
    cancelToken: options.cancelToken,
    xsrfCookieName: options.xsrfCookieName,
    xsrfHeaderName: options.xsrfHeaderName,
  });
}

export function get<T = any>(urlTemplate: string, options: GetRequestOptions = {}): Promise<T> {
  return request(urlTemplate, {
    ...options,
    method: 'GET',
  });
}

export function post<T = any>(urlTemplate: string, options: RequestOptions = {}): Promise<T> {
  return request(urlTemplate, {
    ...options,
    method: 'POST',
  });
}

export function put<T = any>(urlTemplate: string, options: RequestOptions = {}): Promise<T> {
  return request(urlTemplate, {
    ...options,
    method: 'PUT',
  });
}

export function patch<T = any>(urlTemplate: string, options: RequestOptions = {}): Promise<T> {
  return request(urlTemplate, {
    ...options,
    method: 'PATCH',
  });
}

export function delet<T = any>(urlTemplate: string, options: RequestOptions = {}): Promise<T> {
  return request(urlTemplate, {
    ...options,
    method: 'DELETE',
  });
}
