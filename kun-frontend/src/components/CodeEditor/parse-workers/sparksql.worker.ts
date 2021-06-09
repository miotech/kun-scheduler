import { tokenize } from '@/components/CodeEditor/language/sparksql-tokenizer';
import { fetchSQLHintForDatabases, fetchSQLHintForTables } from '@/services/code-hint/sql-hint';

// eslint-disable-next-line no-restricted-globals
const ctx: Worker = self as any;

export enum CompletionItemKind {
  Method = 0,
  Function = 1,
  Constructor = 2,
  Field = 3,
  Variable = 4,
  Class = 5,
  Struct = 6,
  Interface = 7,
  Module = 8,
  Property = 9,
  Event = 10,
  Operator = 11,
  Unit = 12,
  Value = 13,
  Constant = 14,
  Enum = 15,
  EnumMember = 16,
  Keyword = 17,
  Text = 18,
  Color = 19,
  File = 20,
  Reference = 21,
  Customcolor = 22,
  Folder = 23,
  TypeParameter = 24,
  User = 25,
  Issue = 26,
  Snippet = 27
}

const PATTERN_FROM = /from(\s+)(?:\s*\S+\s*(?:\s+(?:as\s+)?\S+\s*)?,\s*)*([^\s.]*)$/i;

const PATTERN_FROM_WITH_SCHEMA = /from(\s+)(?:\s*\S+\s*(?:\s+(?:as\s+)?\S+\s*)?,\s*)*(\S+)\.(\S*)$/i;

const PATTERN_SELECT = /select(\s)+(\S)*$/i;

async function provideDatabaseNames(prefix: string = '') {
  try {
    if (prefix?.length) {
      return await fetchSQLHintForDatabases(prefix);
    }
    return await fetchSQLHintForDatabases();
  } catch (e) {
    return [];
  }
}

async function provideTableNames(databaseName: string, prefix: string = '') {
  try {
    if (prefix?.length) {
      return await fetchSQLHintForTables(databaseName, prefix);
    }
      return await fetchSQLHintForTables(databaseName);

  } catch (e) {
    return [];
  }
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
async function provideColumnNames(prefix = ''): Promise<string[]> {
  // TODO: figure out how to provide column names
  return [];
}

async function suggestion(textBeforeCursor: string, fullText: string, range: any) {
  if (!fullText) {
    return [];
  }
  try {
    const patternFromMatchResult = textBeforeCursor.match(PATTERN_FROM);
    if (patternFromMatchResult) {
      const dbNamePrefix = patternFromMatchResult[2];
      const dbNames = await provideDatabaseNames(dbNamePrefix || '');
      return dbNames.map(token => ({
        label: { name: token.toLowerCase(), type: 'Dataset' },
        kind: CompletionItemKind.Folder,
        insertText: token.toLowerCase(),
        range,
      }));
    }
    const patternFromWithSchemaMatchResult = textBeforeCursor.match(PATTERN_FROM_WITH_SCHEMA);
    if (patternFromWithSchemaMatchResult) {
      const dbName = patternFromWithSchemaMatchResult[2];
      const prefix = patternFromWithSchemaMatchResult[3];
      const tableNames = await provideTableNames(dbName || '', prefix || '');
      return tableNames.map(token => ({
        label: { name: token.toLowerCase(), type: 'Table' },
        kind: CompletionItemKind.Struct,
        insertText: token.toLowerCase(),
        range,
      }));
    }
    if (textBeforeCursor.match(PATTERN_SELECT)) {
      const columnNames = await provideColumnNames();
      return columnNames.map(token => ({
        label: { name: token.toLowerCase(), type: 'Column' },
        kind: CompletionItemKind.Field,
        insertText: token.toLowerCase(),
        range,
      }));
    }
  } catch (e) {
    // do nothing
  }
  // else
  return [];
}

ctx.addEventListener('message', async (event) => {
  const sql = event.data.sql as string;
  const lastWord = event.data.lastWord as string;
  const textBeforeCursor = event.data.textBeforeCursor as string;
  const range = event.data.range as any;
  try {
    const suggestions = await suggestion(textBeforeCursor, sql, range);
    const tokens = tokenize(sql);
    // console.log('tokens =', tokens);
    ctx.postMessage({
      tokens: tokens.filter(t => (t.text != null) && (t.text.trim().length > 0)).map(t => t.text),
      textBeforeCursor,
      suggestions,
      lastWord,
      range,
      error: null,
    });
  } catch (e) {
    ctx.postMessage({
      tokens: null,
      textBeforeCursor,
      suggestions: [],
      lastWord,
      range,
      error: e,
    });
  }
});
