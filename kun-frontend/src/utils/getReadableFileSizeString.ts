/**
 * 得到可读的文件大小
 * @param fileSizeInBytes
 */
const getReadableFileSizeString = (fileSizeInBytes: number) => {
  let tempSize = fileSizeInBytes;
  let i = -1;
  const byteUnits = [' kB', ' MB', ' GB', ' TB', 'PB', 'EB', 'ZB', 'YB'];
  do {
    tempSize /= 1024;
    i += 1;
  } while (tempSize > 1024);

  return Math.max(tempSize, 0.1).toFixed(1) + byteUnits[i];
};

export default getReadableFileSizeString;
