import { Atom, swap, deref } from '@dbeining/react-atom';
import { User } from '@/definitions/User.type';
import { fetchUsersList } from '@/services/user';

export interface UsernameTextSharedState {
  initCount: number;
  loading: boolean;
  userList: User[];
  error: Error | null;
}

export const usernameTextSharedState = Atom.of<UsernameTextSharedState>({
  initCount: 0,
  userList: [],
  loading: false,
  error: null,
});

export const addCount = () => {
  swap(usernameTextSharedState, (prevState) => ({
    ...prevState,
    initCount: prevState.initCount + 1,
  }));
};

export const setSharedUserList = (userList: User[]) => {
  swap(usernameTextSharedState, (prevState) => ({
    ...prevState,
    userList,
  }));
};

export const setSharedLoadingState = (loading: boolean) => {
  swap(usernameTextSharedState, (prevState) => ({
    ...prevState,
    loading,
  }));
};

export const setSharedError = (error: Error | null) => {
  swap(usernameTextSharedState, (prevState) => ({
    ...prevState,
    error,
  }));
};

export const initUserListData = async () => {
  const { initCount } = deref(usernameTextSharedState);
  if (initCount === 0) {
    addCount();
    setSharedLoadingState(true);
    try {
      const result = await fetchUsersList();
      setSharedUserList(result || []);
      setSharedError(null);
    } catch (err) {
      setSharedError(err);
    } finally {
      setSharedLoadingState(false);
    }
  }
};
