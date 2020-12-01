export enum SupportedBrowsers {
  CHROME = 'CHROME',
  IE11 = 'IE11',
  EDGE = 'EDGE',
  HEADLESS = 'HEADLESS',
}

export function detectIE() {
  const ua = window.navigator.userAgent;

  const msie = ua.indexOf('MSIE ');
  if (msie > 0) {
    // IE 10 or older => return version number
    return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
  }

  const trident = ua.indexOf('Trident/');
  if (trident > 0) {
    // IE 11 => return version number
    const rv = ua.indexOf('rv:');
    return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
  }

  const edge = ua.indexOf('Edge/');
  if (edge > 0) {
    // Edge (IE 12+) => return version number
    return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
  }

  // other browser
  return false;
}

export const supportedBrowsers: SupportedBrowsers[] = [
  SupportedBrowsers.CHROME,
  SupportedBrowsers.EDGE,
  SupportedBrowsers.HEADLESS,
];

export function isIE11() {
  return detectIE() === 11;
}

export function isEdge() {
  return detectIE() > 11;
}

export function isHeadless() {
  return Boolean(navigator.webdriver);
}

export function isChrome() {
  const isChromium = Boolean(window.chrome);
  const isIOSChrome = window.navigator.userAgent.match('CriOS');
  const isOpera = typeof window.opr !== 'undefined';

  return (isChromium || isIOSChrome) && isOpera === false && isEdge() === false;
}

export function isBrowserSupported(browserKey: SupportedBrowsers) {
  switch (browserKey) {
    case 'CHROME':
      return isChrome();
    case 'IE11':
      return isIE11();
    case 'EDGE':
      return isEdge();
    case 'HEADLESS':
      return isHeadless();
    default:
      return false;
  }
}

export function isMacintosh(): boolean {
  return navigator.platform.indexOf('Mac') >= 0;
}
