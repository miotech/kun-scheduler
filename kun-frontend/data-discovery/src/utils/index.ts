import moment from 'moment';

export const watermarkFormatter = (time?: string | number) => {
  if (time && !Number.isNaN(Number(time))) {
    return moment(Number(time)).format('YYYY-MM-DD HH:mm:ss');
  }
  return '';
};
