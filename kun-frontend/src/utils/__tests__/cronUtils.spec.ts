import { dateToOneShotCronExpression } from '../cronUtils';

describe('cronUtils: dateToOneShotCronExpression', () => {
  const realDateNow = global.Date.now;
  const realGetTimezoneOffset = global.Date.prototype.getTimezoneOffset;

  beforeAll(() => {
    global.Date.now = jest.fn(() => new Date('2020-10-31 08:30:00+0800').getTime());
    global.Date.prototype.getTimezoneOffset = jest.fn(() => -480);
  });

  afterAll(() => {
    global.Date.now = realDateNow;
    global.Date.prototype.getTimezoneOffset = realGetTimezoneOffset;
  });

  it('should parse date to quartz cron expression properly', () => {
    const inputDate1 = new Date('2020-10-31 11:30:00+0800');
    const parsedCronExpr1 = dateToOneShotCronExpression(inputDate1, { ignoreSeconds: true, utcOffsetHours: 8 });
    expect(parsedCronExpr1).toBe('0 30 11 31 10 ? 2020');

    const inputDate2 = new Date('2020-10-31 11:30:00+0000');
    const parsedCronExpr2 = dateToOneShotCronExpression(inputDate2, { ignoreSeconds: true, utcOffsetHours: 0 });
    expect(parsedCronExpr2).toBe('0 30 11 31 10 ? 2020');

    // should be able to parse by utc time offset
    const inputDate3 = new Date('2020-10-31 11:30:00+0800');
    const parsedCronExpr3 = dateToOneShotCronExpression(inputDate3, { ignoreSeconds: true, utcOffsetHours: 0 });
    expect(parsedCronExpr3).toBe('0 30 3 31 10 ? 2020');
  });

  it('should parse by system default utc offset when `utcOffsetHours` not set', () => {
    const inputDate4 = new Date('2020-10-31 03:30:00+0000');
    const parsedCronExpr4 = dateToOneShotCronExpression(inputDate4);
    // if default system offset hour is UTC+8, then it should do conversion automatically (03:30+00:00 -> 11:30+08:00)
    expect(parsedCronExpr4).toBe('0 30 11 31 10 ? 2020');

    const inputDate5 = new Date('2020-10-31 00:30:00+1200');
    const parsedCronExpr5 = dateToOneShotCronExpression(inputDate5);
    expect(parsedCronExpr5).toBe('0 30 20 30 10 ? 2020');
  });

  it('should accept timestamp as input', () => {
    const inputDate6 = new Date('2020-10-31 11:30:00+0800');
    const inputDate6Timestamp = inputDate6.getTime();
    const parsedCronExpr6 = dateToOneShotCronExpression(inputDate6Timestamp);
    expect(parsedCronExpr6).toBe('0 30 11 31 10 ? 2020');
  });
});
