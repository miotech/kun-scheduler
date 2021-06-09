import { DEFAULT_API_PREFIX } from '@/constants/api-prefixes';
import { simpleGet, simplePost } from '@/utils/simple-ajax-utils';

/**
 * Fetch sql code hint for databases / schemas
 * @param prefix
 */
export async function fetchSQLHintForDatabases(prefix?: string) {
  return simpleGet<string[]>(`${DEFAULT_API_PREFIX}/metadata/dataset/database/_hint`,  { prefix });
}

/**
 * Fetch SQL code hint for tables under corresponding databases / schemas
 * @param databaseName
 * @param prefix
 */
export async function fetchSQLHintForTables(databaseName: string, prefix?: string) {
  return simpleGet<string[]>(`${DEFAULT_API_PREFIX}/metadata/dataset/table/_hint`, {
    databaseName,
    prefix,
  });
}

/**
 * Fetch SQL code hint for columns under corresponding [schema/db].[tableName]
 * @param databaseName
 * @param tableName
 * @param prefix
 */
export async function fetchSQLHintForColumns(databaseName: string, tableName: string, prefix: string = '') {
  return simplePost<string[]>(`${DEFAULT_API_PREFIX}/metadata/dataset/column/_hint`, {
    databaseName,
    tableName,
    prefix,
  });
}
