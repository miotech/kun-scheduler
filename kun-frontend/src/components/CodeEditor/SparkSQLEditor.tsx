/* eslint-disable no-underscore-dangle */
import React, { memo, MutableRefObject, useCallback, useLayoutEffect, useRef } from 'react';
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

  const handleChangeModelContent = useCallback((e, editor) => {
    if (e.text === ' ') {
      editor.trigger('source - use any string you like', 'editor.action.triggerSuggest', {});
    }
  }, []);

  useLayoutEffect(() => {
    if (!workerRef.current) {
      // Here we use worker-plugin (https://github.com/GoogleChromeLabs/worker-plugin) to create a web worker dynamically.
      // Maybe we should switch to Webpack 5 later since it natively support bundling workers.
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
      onDidChangeModelContent={handleChangeModelContent}
    />
  );
});
