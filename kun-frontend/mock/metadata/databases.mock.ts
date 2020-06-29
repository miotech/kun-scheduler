import { API_DATA_PLATFORM_PREFIX } from '../../mock-commons/constants/prefix';

export function fetchDatabases() {
  return [
    {
      "id": "1",
      "name": "company_table"
    },
    {
      "id": "2",
      "name": "people_table"
    }
  ];
}

export default {
  [`GET ${API_DATA_PLATFORM_PREFIX}/metadata/databases/search`]: fetchDatabases,
};
