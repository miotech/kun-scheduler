import difference from 'lodash/difference';
import uniq from 'lodash/uniq';
import pullAll from 'lodash/pullAll';

/**
 * Row selection props factory for antd tables which loads page async
 * @see https://github.com/ant-design/ant-design/issues/2764 for why we made this utility function.
 * @param dataSource
 * @param rowKeyFunc
 * @param selectedRowKeys
 * @param setSelectedRowKeys
 */
export function generateAsyncAntdTableRowSelectionProps<T>(
  dataSource: T[] | null | undefined,
  rowKeyFunc: (record: T) => string | number,
  selectedRowKeys: any[],
  setSelectedRowKeys: (nextKeys: any[]) => any,
) {
  const keysCurrentPage = (dataSource || []).map(rowKeyFunc);
  return {
    selectedRowKeys,
    onChange: (selectedRowKeysCurrentPage: any[]) => {
      const keysToAdd = selectedRowKeysCurrentPage;
      const keysToRemove = difference(keysCurrentPage, keysToAdd);
      const keysRemaining = [...selectedRowKeys];
      pullAll(keysRemaining, keysToRemove);
      // do update
      const nextSelectedKeysState = uniq([...keysToAdd, ...keysRemaining]);
      setSelectedRowKeys(nextSelectedKeysState);
    },
  };
}
