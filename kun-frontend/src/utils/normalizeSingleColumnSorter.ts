import { SorterResult } from 'antd/es/table/interface';
import LogUtils from '@/utils/logUtils';
import isArray from 'lodash/isArray';

const logger = LogUtils.getLoggers('normalizeSingleColumnSorter');

/**
 * Converts ant-design table sorter (single-column only) to API-compatible interface.
 * Will not work in multiple sorter mode.
 * @param sorter
 */
export default function normalizeSingleColumnSorter<T>(sorter: SorterResult<T> | SorterResult<T>[]) {
  let sortColumn = null;
  let sortOrder: 'ASC' | 'DESC' | null = null;

  if (!isArray(sorter)) {
    sortColumn = sorter.column ? `${sorter.columnKey}` : null;
    // eslint-disable-next-line no-nested-ternary
    sortOrder = (typeof sorter.order === 'string') ? (sorter.order === 'ascend' ? 'ASC' : 'DESC') : null;
  } else {
    logger.warn('Unexpected sorter value: %o', sorter);
  }

  return {
    sortColumn,
    sortOrder,
  };
}
