const FULL_CHARTER = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopgrstuvwxyz';

// 应用id
const clientId = '***REMOVED***';
// 授权中心地址
const uaaUri = 'https://dev-7959592.okta.com/oauth2/v1/';

export function getState() {
  let state = '';
  for (let i = 0; i < 16; i += 1) {
    state += FULL_CHARTER[Math.floor(Math.random() * 52)];
  }
  return state;
}

export function getAuthorizeUri(state: string) {
  return `${uaaUri}authorize?client_id=${clientId}&redirect_uri=http://${window.location.host}/sso&response_type=code&state=${state}&scope=openid%20profile%20email`;
}

/**
 * 获取url参数
 */
export function getQueryVariable(variable: string) {
  const query = window.location.search.substring(1);
  const vars = query.split('&');
  for (let i = 0; i < vars.length; i += 1) {
    const pair = vars[i].split('=');
    if (pair[0] === variable) {
      return pair[1];
    }
  }
  return '';
}
