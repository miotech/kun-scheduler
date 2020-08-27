import { RootDispatch, RootState } from '@/rematch/store';
import { useDispatch, useSelector, shallowEqual } from 'react-redux';

export default function useRedux<T>(selectorFunc: (state: RootState) => T) {
  const dispatch: RootDispatch = useDispatch<RootDispatch>();
  const selector: T = useSelector(selectorFunc, shallowEqual);
  return { selector, dispatch };
}
