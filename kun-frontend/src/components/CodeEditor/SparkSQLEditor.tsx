/* eslint-disable no-underscore-dangle */
import React, { memo, useLayoutEffect } from 'react';
import { DefaultMonacoEditor } from '@/components/CodeEditor/DefaultMonacoEditor';
import { useMonaco } from '@monaco-editor/react';
import { initSQLLanguageSupport } from '@/components/CodeEditor/language/sql';

interface OwnProps {
  value?: string;
  onChange?: (nextValue?: string, ev?: any) => any;
  defaultValue?: string;
  theme?: string;
}

type Props = OwnProps;

export const SparkSQLEditor: React.FC<Props> = memo(function SparkSQLEditor(props) {
  const { defaultValue, value, theme, onChange } = props;

  const monaco = useMonaco();

  useLayoutEffect(() => {
    if (monaco) {
      initSQLLanguageSupport(monaco);
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
