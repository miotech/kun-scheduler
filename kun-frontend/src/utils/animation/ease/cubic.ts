export function cubicIn(t: number) {
  return t * t * t;
}

export function cubicOut(t: number): number {
  // eslint-disable-next-line no-param-reassign,no-plusplus
  return --t * t * t + 1;
}

export function cubicInOut(t: number): number {
  // eslint-disable-next-line no-cond-assign,no-param-reassign
  return ((t *= 2) <= 1 ? t * t * t : (t -= 2) * t * t + 2) / 2;
}
