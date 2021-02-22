import { Atom, swap } from '@dbeining/react-atom';
import { GlobalVariable } from '@/definitions/GlobalVariable.type';
import LogUtils from '@/utils/logUtils';
import { fetchGlobalVariablesList } from '@/services/variables/variables.services';

const logger = LogUtils.getLoggers('VariableSettingsViewState');

export interface VariableSettingsViewState {
  searchKeyword: string;
  variableList: GlobalVariable[];
  variableListIsLoading: boolean;
}

export const viewState = Atom.of<VariableSettingsViewState>({
  searchKeyword: '',
  variableList: [],
  variableListIsLoading: false,
});

/* Reducers */

export function setList(nextListState: GlobalVariable[]) {
  swap(viewState, prevState => ({
    ...prevState,
    variableList: nextListState,
  }));
}

export function setListLoading(nextListLoadingState: boolean) {
  swap(viewState, prevState => ({
    ...prevState,
    variableListIsLoading: nextListLoadingState,
  }));
}

export function setSearchKeyword(nextKeyword: string) {
  swap(viewState, prevState => ({
    ...prevState,
    searchKeyword: nextKeyword,
  }));
}

/* Effects */

export async function fetchAndLoadVariablesList() {
  setListLoading(true);
  try {
    const listData = await fetchGlobalVariablesList();
    if (listData) {
      setList(listData);
    }
  } catch (e) {
    logger.error(e);
  } finally {
    setListLoading(false);
  }
}
