export type SQLQueryResult = {
  msg: string | null;
  errorMsg: string | null;
  total: string | number;
  hasNext: boolean;
  columnNames: string[];
  records: SQLQueryRow[];
};

export type SQLQueryRow = (string | null)[];

export type SQLQueryTab = {
  id: string;
  ordinal: number;
  sql: string;
  response: SQLQueryResult | null;
  done: boolean;
}
