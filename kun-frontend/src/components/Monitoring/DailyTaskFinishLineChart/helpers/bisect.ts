import cloneDeep from 'lodash/cloneDeep';

interface CompareFn<T> {
  (a: T, b: T): number;
}

function isNil(x: any): x is (null | undefined) {
  return x == null;
}

function ascending(a: number, b: number): number {
  if (a < b) {
    return -1;
  }
  if (a > b) {
    return 1;
  }
  if (a >= b) {
    return 0;
  }
  return NaN;
}

function left<T>(arr: T[], x: T, compare: CompareFn<T>, fromIndex?: number, toIndex?: number): number {
  let low: number = isNil(fromIndex) ? 0 : fromIndex;
  let high: number = isNil(toIndex) ? arr.length : toIndex;
  // perform binary search
  while (low < high) {
    // eslint-disable-next-line no-bitwise
    const mid = (low + high) >>> 1;
    if (compare(arr[mid], x) < 0) {
      low = mid + 1;
    } else {
      high = mid;
    }
  }
  return low;
}

export function bisectCenter(searchArray: number[], x: number, alreadySorted = true): number {
  let arr = searchArray;
  if (!alreadySorted) {
    arr = cloneDeep(searchArray);
    arr.sort();
  }
  const i = left(arr, x, ascending);
  if ((i > 0) && (Math.abs(arr[i - 1] - x) <= Math.abs(arr[i] - x))) {
    return i - 1;
  }
  return i;
}
