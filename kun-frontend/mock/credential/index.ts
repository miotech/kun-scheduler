import { getWhoAmI, mockFetchUsersList } from './whoami';
import { DEFAULT_API_PREFIX } from '../../mock-commons/constants/prefix';

export default {
  [`GET ${DEFAULT_API_PREFIX}/user/whoami`]: getWhoAmI,
  [`GET ${DEFAULT_API_PREFIX}/user/list`]: mockFetchUsersList,
};
