import { simplePost } from '@/utils/simple-ajax-utils';
import { API_DATA_PLATFORM_PREFIX } from '@/constants/api-prefixes';
import { SQLQueryResult } from '@/definitions/QueryResult.type';

/**
 * Dry run a segment of SQL code to fetch result
 * @param parameters
 */
export async function doSQLExecute(parameters: {
  sql: string;
  pageNum: number;
  pageSize: number;
}) {
  return simplePost<SQLQueryResult>(`${API_DATA_PLATFORM_PREFIX}/spark-sql/_execute`,  { ...parameters });
}
