/* eslint-disable no-underscore-dangle */
import React, { memo, MutableRefObject, useLayoutEffect, useRef } from 'react';
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

  const workerRef = useRef(null) as MutableRefObject<Worker | null>;

  const monaco = useMonaco();

  useLayoutEffect(() => {
    if (!workerRef.current) {
      workerRef.current = new Worker('./parse-workers/sparksql.worker.ts', { type: 'module' });
    }
    if (monaco) {
      initSQLLanguageSupport(monaco, workerRef.current);
    }
    return () => {
      if (workerRef.current != null) {
        workerRef.current.terminate();
        workerRef.current = null;
      }
    };
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
