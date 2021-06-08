/* eslint-disable no-underscore-dangle */
import { Monaco } from '@monaco-editor/react';
// eslint-disable-next-line import/no-unresolved
import { editor, IRange, languages, Position } from 'monaco-editor';
import { message } from 'antd';
import { format as sqlFormat } from 'sql-formatter';

function doSQLFormat(sql: string): Promise<string> {
  try {
    const formattedSQL = sqlFormat(sql, {
      language: 'spark',
    });
    return Promise.resolve(formattedSQL);
  } catch (e) {
    return Promise.reject(e.message);
  }
}

const KEYWORD_LIST = [
  'SELECT', 'AS', 'FROM', 'WHERE', 'INNER JOIN', 'LEFT JOIN', 'RIGHT JOIN', 'OUTER JOIN',
  'ON', 'WITH', 'AGGREGATE', 'ORDER BY', 'ASC', 'DESC', 'CREATE', 'INSERT', 'INTO', 'UPDATE', 'VALUES', 'RECURSIVE'
];

const OPERATORS = [
  "ALL", "AND", "ANY", "BETWEEN", "EXISTS", "IN", "LIKE", "NOT", "OR", "SOME",
  "EXCEPT", "INTERSECT", "UNION", "APPLY", "CROSS", "FULL", "INNER", "JOIN", "LEFT",
  "OUTER", "RIGHT", "CONTAINS", "IS", "NULL", "PIVOT", "UNPIVOT", "MATCHED"
];

const BUILTIN_FUNCTIONS = [
  "abs","acos","acosh","add_months","aggregate","and","any","approx_count_distinct","approx_percentile","array","array_contains",
  "array_distinct","array_except","array_intersect","array_join","array_max","array_min","array_position","array_remove","array_repeat",
  "array_sort","array_union","arrays_overlap","arrays_zip","ascii","asin","asinh","assert_true","atan","atan2","atanh","avg","base64",
  "between","bigint","bin","binary","bit_and","bit_count","bit_length","bit_or","bit_xor","bool_and","bool_or","boolean","bround",
  "cardinality","case","cast","cbrt","ceil","ceiling","char","char_length","character_length","chr","coalesce","collect_list",
  "collect_set","concat","concat_ws","conv","corr","cos","cosh","cot","count","count_if","count_min_sketch","covar_pop","covar_samp",
  "crc32","cube","cume_dist","current_catalog","current_database","current_date","current_timestamp","current_timezone","date",
  "date_add","date_format","date_from_unix_date","date_part","date_sub","date_trunc","datediff","day","dayofmonth","dayofweek",
  "dayofyear","decimal","decode","degrees","dense_rank","div","double","e","element_at","elt","encode","every","exists","exp",
  "explode","explode_outer","expm1","extract","factorial","filter","find_in_set","first","first_value","flatten","float","floor",
  "forall","format_number","format_string","from_csv","from_json","from_unixtime","from_utc_timestamp","get_json_object","greatest",
  "grouping","grouping_id","hash","hex","hour","hypot","if","ifnull","in","initcap","inline","inline_outer","input_file_block_length",
  "input_file_block_start","input_file_name","instr","int","isnan","isnotnull","isnull","java_method","json_array_length","json_object_keys",
  "json_tuple","kurtosis","lag","last","last_day","last_value","lcase","lead","least","left","length","levenshtein","like","ln",
  "locate","log","log10","log1p","log2","lower","lpad","ltrim","make_date","make_interval","make_timestamp","map","map_concat",
  "map_entries","map_filter","map_from_arrays","map_from_entries","map_keys","map_values","map_zip_with","max","max_by","md5",
  "mean","min","min_by","minute","mod","monotonically_increasing_id","month","months_between","named_struct","nanvl","negative",
  "next_day","not","now","nth_value","ntile","nullif","nvl","nvl2","octet_length","or","overlay","parse_url","percent_rank",
  "percentile","percentile_approx","pi","pmod","posexplode","posexplode_outer","position","positive","pow","power","printf","quarter",
  "radians","raise_error","rand","randn","random","rank","reflect","regexp_extract","regexp_extract_all","regexp_replace","repeat",
  "replace","reverse","right","rint","rlike","rollup","round","row_number","rpad","rtrim","schema_of_csv","schema_of_json","second",
  "sentences","sequence","sha","sha1","sha2","shiftleft","shiftright","shiftrightunsigned","shuffle","sign","signum","sin","sinh",
  "size","skewness","slice","smallint","some","sort_array","soundex","space","spark_partition_id","split","sqrt","stack","std","stddev",
  "stddev_pop","stddev_samp","str_to_map","string","struct","substr","substring","substring_index","sum","tan","tanh","timestamp",
  "timestamp_micros","timestamp_millis","timestamp_seconds","tinyint","to_csv","to_date","to_json","to_timestamp","to_unix_timestamp",
  "to_utc_timestamp","transform","transform_keys","transform_values","translate","trim","trunc","typeof","ucase","unbase64","unhex",
  "unix_date","unix_micros","unix_millis","unix_seconds","unix_timestamp","upper","uuid","var_pop","var_samp","variance","version","weekday",
  "weekofyear","when","width_bucket","window","xpath","xpath_boolean","xpath_double","xpath_float","xpath_int","xpath_long","xpath_number",
  "xpath_short","xpath_string","xxhash64","year","zip_with"
];

function getKeywordsAndFunctionsList(range: IRange): languages.CompletionItem[] {
  const keywords = KEYWORD_LIST.map(token => ({
    label: token,
    kind: languages.CompletionItemKind.Keyword,
    insertText: token,
    range,
  }));

  const keywordsLowerCase = KEYWORD_LIST.map(token => ({
    label: token.toLowerCase(),
    kind: languages.CompletionItemKind.Keyword,
    insertText: token.toLowerCase(),
    range,
  }));

  const operators = OPERATORS.map(token => ({
    label: token,
    kind: languages.CompletionItemKind.Operator,
    insertText: token,
    range,
  }));

  const operatorsLowerCase = OPERATORS.map(token => ({
    label: token.toLowerCase(),
    kind: languages.CompletionItemKind.Operator,
    insertText: token.toLowerCase(),
    range,
  }));

  const builtInFunctions = BUILTIN_FUNCTIONS.map(token => ({
    label: token,
    kind: languages.CompletionItemKind.Function,
    insertText: token,
    range,
  }));


  return [...keywords, ...keywordsLowerCase, ...operators, ...operatorsLowerCase, ...builtInFunctions];
}

export function initSQLLanguageSupport(monaco: Monaco, sqlLanguageWorker: Worker | null = null) {
  // @ts-ignore
  if (!window.__MONACO_EDITOR_SQL_SYNTAX_REGISTERED__) {
    // @ts-ignore
    window.__MONACO_EDITOR_SQL_SYNTAX_REGISTERED__ = true;

    monaco.languages.registerDocumentFormattingEditProvider('sql', {
      async provideDocumentFormattingEdits(model) {
        const preFormatCode = model.getValue();
        try {
          const formatted = await doSQLFormat(preFormatCode);
          return [
            {
              range: model.getFullModelRange(),
              text: formatted,
            },
          ];
        } catch (e) {
          message.error('Failed to format code.');
          return [
            {
              range: model.getFullModelRange(),
              text: preFormatCode,
            },
          ];
        }
      }
    });

    monaco.languages.registerCompletionItemProvider('sql', {
      provideCompletionItems(
        model: editor.ITextModel,
        position: Position,
        // context: languages.CompletionContext,
        // token: CancellationToken,
      ): languages.ProviderResult<languages.CompletionList> {
        const word = model.getWordUntilPosition(position);
        const range = {
          startLineNumber: position.lineNumber,
          endLineNumber: position.lineNumber,
          startColumn: word.startColumn,
          endColumn: word.endColumn
        } as IRange;

        return new Promise((resolve, reject) => {
          if (sqlLanguageWorker != null) {
            // eslint-disable-next-line
            sqlLanguageWorker.onmessage = function sqlLanguageWorkerOnMessage(ev) {
              if (ev.data.suggestions) {
                resolve({
                  suggestions: [
                    ...ev.data.suggestions,
                    ...getKeywordsAndFunctionsList(range),
                  ],
                });
              } else if (ev.data.error) {
                reject(ev.data.error);
              }
              // finally: remove this listener
              sqlLanguageWorker.removeEventListener('message', sqlLanguageWorkerOnMessage);
            };
            // Do message posting
            sqlLanguageWorker.postMessage({
              sql: model.getValue(),
              textBeforeCursor: model.getValueInRange({
                startLineNumber: 0,
                endLineNumber: position.lineNumber,
                startColumn: 0,
                endColumn: position.column,
              }),
              lastWord: model.getWordUntilPosition(position).word,
              position,
              range,
            });
          } else {
            return resolve({
              suggestions: getKeywordsAndFunctionsList(range),
            });
          }
          return null;
        });
        // return Promise.resolve({
        //   suggestions: [...getKeywordsAndFunctionsList(range)],
        // });
      },
    });
  }
}
