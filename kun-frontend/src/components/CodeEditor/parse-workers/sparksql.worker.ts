import { tokenize } from '@/components/CodeEditor/language/sparksql-tokenizer';

// eslint-disable-next-line no-restricted-globals
const ctx: Worker = self as any;

enum CompletionItemKind {
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

const PATTERN_FROM = /from(\s)+(\S)*$/i;

const PATTERN_SELECT = /select(\s)+(\S)*$/i;

async function provideTableNames(prefix: string = '') {
  if (!prefix) {
    return [
      'kun_dp_deploy',
      'kun_dp_backfill',
      'kun_wf_tasks',
      'kun_wf_taskruns',
    ];
  }
  // else
  return [];
}

async function provideColumnNames(prefix = '') {
  if (!prefix) {
    return [
      'id',
      'name',
      'create_at',
      'update_at',
    ];
  }
  // else
  return [];
}

async function suggestion(textBeforeCursor: string, fullText: string, range: any) {
  if (!fullText) {
    return [];
  }
  try {
    if (textBeforeCursor.match(PATTERN_FROM)) {
      const tableNames = await provideTableNames();
      return tableNames.map(token => ({
        label: token.toLowerCase(),
        kind: CompletionItemKind.File,
        insertText: token.toLowerCase(),
        range,
      }));
    }
    if (textBeforeCursor.match(PATTERN_SELECT)) {
      const columnNames = await provideColumnNames();
      return columnNames.map(token => ({
        label: token.toLowerCase(),
        kind: CompletionItemKind.Property,
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
    console.log('tokens =', tokens);
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
