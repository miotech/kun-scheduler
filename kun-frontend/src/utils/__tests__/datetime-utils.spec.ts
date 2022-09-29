import { dateFormatter } from '../datetime-utils';

describe('datetime-utils: dateFormatter', () => {
  it('should return "-"', () => {
    const expectedResult = '-';
    expect(dateFormatter(0)).toEqual(expectedResult);
    expect(dateFormatter('')).toEqual(expectedResult);
  });
  it('should return formatted datetime', () => {
    expect(dateFormatter('2022-07-2')).toEqual('2022-07-03 00:00');
  });
});
