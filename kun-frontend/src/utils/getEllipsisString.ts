/**
 * string过长, 省略中间的字符
 * @param text
 * @param length
 * @param startWordCount
 * @param endWordCount
 */
const ellipsisString = (
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

export default ellipsisString;
