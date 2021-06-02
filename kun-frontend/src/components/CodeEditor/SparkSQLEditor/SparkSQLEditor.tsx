/* eslint-disable no-underscore-dangle */
import React, { memo, useLayoutEffect } from 'react';
import MonacoEditor, { loader, useMonaco } from '@monaco-editor/react';

import '../CodeEditor.less';
import loadMonacoThemes from '@/components/CodeEditor/themes/load-themes';
import { Select } from 'antd';

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
    // @ts-ignore
    if (!window.__MONACO_EDITOR_RECONFIGURED__) {
      // @ts-ignore
      window.__MONACO_EDITOR_RECONFIGURED__ = true;
      loader.config({
        paths: {
          vs: 'https://cdn.bootcdn.net/ajax/libs/monaco-editor/0.23.0/min/vs',
        },
      });
    }
    if (monaco != null) {
      loadMonacoThemes(monaco);
    }
  }, [monaco]);

  return (
    <div className="monaco-editor-wrapper">
      <nav className="monaco-editor-wrapper__toolbar">
        <div className="monaco-editor-wrapper__toolbar__left">
          Content left
        </div>
        <div className="monaco-editor-wrapper__toolbar__right">
          <Select>
            <Select.Option value="light">Light</Select.Option>
            <Select.Option value="light">Light+</Select.Option>
            <Select.Option value="dark">Dark</Select.Option>
            <Select.Option value="light">Monakai</Select.Option>
          </Select>
        </div>
      </nav>
      <div className="monaco-editor-wrapper__editor">
        <MonacoEditor
          value={value}
          onChange={onChange}
          language="sql"
          defaultValue={defaultValue}
          height="450px"
          theme={theme}
        />
      </div>
    </div>
  );
});
