/**
 * Simple AJAX utility functions that compatible with web-workers
 */


const isObject = (it: any) => it != null && typeof it === 'object';

/**
 * Encodes an object to be used as query-string.
 * It uses 'encodeURIComponent' to set each value.
 *
 * @returns {string} the query-string
 * @param queries
 */
function queryToUrlParams(queries: Record<string, any>) {
  let value;
  if (!queries) {
    return '';
  }
  const serialized: string[] = [];
  Object.keys(queries).forEach((key) => {
    value = queries[key];
    if (value === undefined) return; // continue;
    if (typeof value === 'function') return;
    if (isObject(value)) {
      value = JSON.stringify(value);
    }
    // key and value should be decoded with decodeURIComponent
    serialized.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
  });
  return `?${serialized.join('&')}`;
}

export function simpleGet<T>(url: string, query?: Record<string, any>): Promise<T> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const finalUrl = (query == null) ? url : url + queryToUrlParams(query);
    xhr.open('GET', finalUrl, true);
    xhr.setRequestHeader('Content-Type', 'application/json;charset=utf-8');
    xhr.withCredentials = true;
    xhr.onreadystatechange = function onReadyStateChange() {
      if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
        // Request finished. Do processing here.
        const data = JSON.parse(this.responseText);
        resolve(data.result);
      } else if (this.readyState === XMLHttpRequest.DONE && this.status >= 400) {
        const data = JSON.parse(this.responseText);
        reject(data);
      }
    };
    xhr.send();
  });
}

export function simplePost<T>(url: string, bodyData?: Record<string, any>, query?: Record<string, any>): Promise<T> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const finalUrl = (query == null) ? url : url + queryToUrlParams(query);
    const postData = JSON.stringify(bodyData);
    xhr.open('POST', finalUrl, true);
    xhr.setRequestHeader('Content-Type', 'application/json;charset=utf-8');
    xhr.withCredentials = true;
    xhr.onreadystatechange = function onReadyStateChange() {
      if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
        // Request finished. Do processing here.
        const data = JSON.parse(this.responseText);
        resolve(data.result);
      } else if (this.readyState === XMLHttpRequest.DONE && this.status >= 400) {
        const data = JSON.parse(this.responseText);
        reject(data);
      }
    };
    xhr.send(postData);
  });
}

