/* eslint-disable no-underscore-dangle */
import React, { memo, useLayoutEffect } from 'react';
import { DefaultMonacoEditor } from '@/components/CodeEditor/DefaultMonacoEditor';
import { useMonaco } from '@monaco-editor/react';
import { format as sqlFormat } from 'sql-formatter';
import { message } from 'antd';

interface OwnProps {
  value?: string;
  onChange?: (nextValue?: string, ev?: any) => any;
  defaultValue?: string;
  theme?: string;
}

type Props = OwnProps;

function doSQLFormat(sql: string): Promise<string> {
  try {
    const formattedSQL = sqlFormat(sql, {
      language: 'spark',
    });
    return Promise.resolve(formattedSQL);
  } catch (e) {
    return Promise.reject(e.message);
  }
};

export const SparkSQLEditor: React.FC<Props> = memo(function SparkSQLEditor(props) {
  const { defaultValue, value, theme, onChange } = props;

  const monaco = useMonaco();

  useLayoutEffect(() => {
    if (monaco) {
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
    }
  }, [monaco]);

  return (
    <DefaultMonacoEditor
      language="sql"
      value={value}
      defaultValue={defaultValue}
      onChange={onChange}
      theme={theme}
    />
  );
});
