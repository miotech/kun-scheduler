/* eslint-disable no-underscore-dangle */
import React, { memo, useCallback, useLayoutEffect, useRef } from 'react';
import { Button } from 'antd';

import loadMonacoThemes from '@/components/CodeEditor/themes/load-themes';
import { MonacoEditorThemeSelect } from '@/components/CodeEditor/MonacoEditorThemeSelect.component';
import { useStickyState } from '@/hooks/useStickyState';
import MonacoEditor, { loader, useMonaco } from '@monaco-editor/react';
import { MONACO_EDITOR_THEME_LOCALSTORAGE_KEY } from '@/constants/localstorage-keys.const';

// TODO: Seems like an eslint bug?
// eslint-disable-next-line import/no-unresolved
import { editor } from 'monaco-editor';

import './CodeEditor.less';

interface OwnProps {
  value?: string;
  onChange?: (nextValue?: string, ev?: any) => any;
  defaultValue?: string;
  theme?: string;
  language?: string;
}

type Props = OwnProps;

loader.init().then(monaco => {
  loadMonacoThemes(monaco);
});

export const DefaultMonacoEditor: React.FC<Props> = memo(function DefaultMonacoEditor(props) {
  const monaco = useMonaco();
  const editorRef = useRef<editor.IStandaloneCodeEditor>(null);

  const { defaultValue, value, theme: propsTheme, onChange, language } = props;
  const [ themeStored, setTheme ] = useStickyState<string>('light', MONACO_EDITOR_THEME_LOCALSTORAGE_KEY);
  const theme = propsTheme || themeStored;

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
  }, [monaco]);

  const handleEditorDidMount = useCallback(function handleEditorDidMount(editorInstance) {
    // @ts-ignore
    editorRef.current = editorInstance;
  }, []);

  const handleAutoFormat = useCallback(function handleAutoFormat() {
    if (editorRef.current != null) {
      editorRef.current.getAction('editor.action.formatDocument').run();
    }
  }, []);

  return (
    <div className="monaco-editor-wrapper">
      <nav className="monaco-editor-wrapper__toolbar">
        <div className="monaco-editor-wrapper__toolbar__left">
          {/* TODO: toolbar content left side */}
        </div>
        <div className="monaco-editor-wrapper__toolbar__right">
          {/* Auto format */}
          <Button onClick={handleAutoFormat}>
            Auto Format
          </Button>
          {/* Theme selector */}
          <MonacoEditorThemeSelect
            value={theme}
            onChange={(nextVal: string) => {
              setTheme(nextVal);
            }}
          />
        </div>
      </nav>
      <div className="monaco-editor-wrapper__editor">
        <MonacoEditor
          value={value}
          onChange={onChange}
          language={language}
          defaultValue={defaultValue}
          height="450px"
          theme={theme}
          onMount={handleEditorDidMount}
        />
      </div>
    </div>
  );
});
