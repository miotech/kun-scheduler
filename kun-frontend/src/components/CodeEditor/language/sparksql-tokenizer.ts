import { cleanSql, SparkSQL } from 'dt-sql-parser';

export function tokenize(sql: string) {
  const normalizedSQL = cleanSql(sql);
  const parser = new SparkSQL();
  return parser.getAllTokens(normalizedSQL);
}
