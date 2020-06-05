import { useDispatch } from 'react-redux';
import { RootDispatch } from '@/rematch/store';

const useRematchDispatch = <MD>(selector: (dispatch: RootDispatch) => MD) => {
  const dispatch = useDispatch<RootDispatch>();
  return selector(dispatch);
};

export default useRematchDispatch;
