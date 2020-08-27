import uniqueId from 'lodash/uniqueId';

/**
 * 得到唯一id
 * @param namespace
 */
export const getUUID = (namespace: string = 'namespace') => {
  return uniqueId(namespace);
};

/**
 * string过长, 省略中间的字符
 * @param text
 * @param length
 * @param startWordCount
 * @param endWordCount
 */
export const ellipsisString = (
  text?: string,
  length: number = 50,
  startWordCount: number = 15,
  endWordCount: number = 15,
) => {
  if (!text) {
    return '';
  }
  if (text.length <= length) {
    return text;
  }
  const startString = text.substring(0, startWordCount);
  const endString = text.substring(text.length - endWordCount);
  return `${startString}...${endString}`;
};

/**
 * 得到可读的文件大小
 * @param fileSizeInBytes
 */
export const getReadableFileSizeString = (fileSizeInBytes: number) => {
  let tempSize = fileSizeInBytes;
  let i = -1;
  const byteUnits = [' kB', ' MB', ' GB', ' TB', 'PB', 'EB', 'ZB', 'YB'];
  do {
    tempSize /= 1024;
    i += 1;
  } while (tempSize > 1024);

  return Math.max(tempSize, 0.1).toFixed(1) + byteUnits[i];
};

/**
 * 是否有其中之一的权限
 * @param currentPer
 * @param needPer
 */
export const hasOptionalPermissions = (
  currentPer: string[],
  needPer?: string[],
) => {
  if (!needPer) {
    return true;
  }
  let has = false;
  currentPer.forEach(i => {
    if (needPer.includes(i)) {
      has = true;
    }
  });
  return has;
};

/**
 * 判断是否支持全屏
 */
export const isFullscreenEnabled = () => document.fullscreenEnabled;

/**
 * 判断是否全屏
 */
export const isFullscreen = () => document.fullscreenElement;
