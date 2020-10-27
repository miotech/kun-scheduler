// Copied from https://github.com/d3/d3-ease/blob/master/src/quad.js

export function quadIn(t: number): number {
  return t * t;
}

export function quadOut(t: number): number {
  return t * (2 - t);
}

export function quadInOut(t: number): number {
  // eslint-disable-next-line no-cond-assign,no-return-assign,no-param-reassign,no-plusplus
  return ((t *= 2) <= 1 ? t * t : --t * (2 - t) + 1) / 2;
}
