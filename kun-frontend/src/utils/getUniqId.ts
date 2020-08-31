import uniqueId from 'lodash/uniqueId';

/**
 * 得到唯一id
 * @param namespace
 */
const getUniqId = (namespace: string = 'namespace') => {
  return uniqueId(namespace);
};

export default getUniqId;
