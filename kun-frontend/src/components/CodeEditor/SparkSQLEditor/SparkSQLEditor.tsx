/* eslint-disable no-underscore-dangle */
import React, { memo, useLayoutEffect } from 'react';
import MonacoEditor, { loader } from '@monaco-editor/react';

interface OwnProps {
  value?: string;
  defaultValue?: string;
  theme?: string;
}

type Props = OwnProps;

export const SparkSQLEditor: React.FC<Props> = memo(function SparkSQLEditor(props) {
  const { defaultValue, value, theme } = props;

  useLayoutEffect(() => {
    // @ts-ignore
    if (!window.__MONACO_EDITOR_RECONFIGED__) {
      // @ts-ignore
      window.__MONACO_EDITOR_RECONFIGED__ = true;
      loader.config({
        paths: {
          vs: 'https://cdn.bootcdn.net/ajax/libs/monaco-editor/0.23.0/min/vs',
        },
      });
    }
  }, []);

  return (
    <MonacoEditor
      value={value}
      language="sql"
      defaultValue={defaultValue}
      height="450px"
      theme={theme}
    />
  );
});
