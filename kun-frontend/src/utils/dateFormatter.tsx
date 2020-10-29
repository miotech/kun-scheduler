import moment from 'moment';

export function dateFormatter(value: number | string) {
  const numberValue = Number(value);
  if (!value || Number.isNaN(numberValue)) {
    return '-';
  }
  return moment(numberValue).format('YYYY-MM-DD');
}
