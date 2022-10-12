/**
 * Convert hex string to rgba
 * @param hex
 * @param opacity
 * @returns
 */
export const convertHex2Rgba = (hex: string, opacity?: number | string) => {
  const len = hex?.length;
  let hexStr = hex;
  if (len !== 3 && len !== 6) {
    console.warn('Please pass valid hex value');
    return `#${hex}`;
  }

  if (len === 3) {
    hexStr = hex
      .split('')
      .map(str => {
        return str.repeat(2);
      })
      .join('');
  }

  const rgbs = hexStr.match(/.{1,2}/g)?.map(str => {
    return parseInt(str, 16);
  });

  if (!rgbs || rgbs.length !== 3) {
    return `#${hex}`;
  }

  return `rgba(${rgbs.join(',')}, ${opacity ?? 1})`;
};
