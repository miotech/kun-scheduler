export interface ReactMoveTiming {
  timing?: {
    duration?: number;
    delay?: number;
  },
}

export interface NodeGroupElement<T = any, StateType extends ReactMoveTiming = ReactMoveTiming, KeyType = string> {
  data: T;
  getInterpolator: (begin: number, end: number, attr?: any) => (t: number) => number;
  key: KeyType;
  state: StateType;
  type: 'ENTER' | 'UPDATE' | 'LEAVE';
}
