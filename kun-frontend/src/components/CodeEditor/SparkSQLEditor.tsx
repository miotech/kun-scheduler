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

  // @ts-ignore
  const workerRef = useRef(window.__MONACO_SQL_HINT_WORKER__ || null) as MutableRefObject<Worker | null>;

  const monaco = useMonaco();

  useLayoutEffect(() => {
    if (!workerRef.current) {
      // @ts-ignore
      if (window.__MONACO_SQL_HINT_WORKER__ == null) {
        // Here we use worker-plugin (https://github.com/GoogleChromeLabs/worker-plugin) to create a web worker dynamically.
        // A created worker will be reused later.
        // Maybe we should switch to Webpack 5 later since it natively support bundling workers.
        // @ts-ignore
        window.__MONACO_SQL_HINT_WORKER__ = new Worker('./parse-workers/sparksql.worker.ts', { type: 'module' });
      }
      // @ts-ignore
      workerRef.current = window.__MONACO_SQL_HINT_WORKER__;
    }
    if (monaco) {
      initSQLLanguageSupport(monaco, workerRef.current);
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
