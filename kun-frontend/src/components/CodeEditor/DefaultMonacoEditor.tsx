/* eslint-disable no-underscore-dangle */
import React, { memo, useCallback, useLayoutEffect, useRef, useState } from 'react';
import { Button } from 'antd';
import { useSize, useFullscreen } from 'ahooks';
import loadMonacoThemes from '@/components/CodeEditor/themes/load-themes';
import { MonacoEditorThemeSelect } from '@/components/CodeEditor/MonacoEditorThemeSelect.component';
import { useStickyState } from '@/hooks/useStickyState';
import Editor, { loader, useMonaco } from '@monaco-editor/react';
import { MONACO_EDITOR_THEME_LOCALSTORAGE_KEY } from '@/constants/localstorage-keys.const';
import { FullscreenOutlined, FullscreenExitOutlined } from '@ant-design/icons';
// TODO: Seems like an eslint bug?
// eslint-disable-next-line import/no-unresolved
import { editor } from 'monaco-editor';
import * as Monaco from 'monaco-editor/esm/vs/editor/editor.api';

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
  const fullScreenRef = useRef(null);
  const [isFullscreen, { enterFullscreen, exitFullscreen }] = useFullscreen(fullScreenRef);
  const monaco = useMonaco();
  const editorRef = useRef<editor.IStandaloneCodeEditor>(null);
  const size = useSize(fullScreenRef);
  let editorHeight = 450;
  if (isFullscreen) {
    editorHeight = size?.height - 80;
  }
  const [cursorPosition, setCursorPosition] = useState<any>();

  const { defaultValue, value, theme: propsTheme, onChange, language } = props;
  const [themeStored, setTheme] = useStickyState<string>('light', MONACO_EDITOR_THEME_LOCALSTORAGE_KEY);
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

  const handleEditorDidMount = useCallback(function handleEditorDidMount(
    editorInstance: Monaco.editor.IStandaloneCodeEditor,
  ) {
    // @ts-ignore
    editorRef.current = editorInstance;
    editorRef.current.onDidChangeCursorPosition((e: Monaco.editor.ICursorPositionChangedEvent) => {
      setCursorPosition(e.position);
    });
  },
  []);

  const handleAutoFormat = useCallback(function handleAutoFormat() {
    if (editorRef.current != null) {
      editorRef.current.getAction('editor.action.formatDocument').run();
    }
  }, []);

  return (
    <div ref={fullScreenRef} className="monaco-editor-wrapper">
      <nav className="monaco-editor-wrapper__toolbar">
        <div className="monaco-editor-wrapper__toolbar__left">{/* TODO: toolbar content left side */}</div>
        <div className="monaco-editor-wrapper__toolbar__right">
          {/* Auto format */}
          <Button style={{ marginRight: '5px' }} onClick={handleAutoFormat}>
            Auto Format
          </Button>
          {/* Theme selector */}
          <MonacoEditorThemeSelect
            value={theme}
            onChange={(nextVal: string) => {
              setTheme(nextVal);
            }}
          />
          {!isFullscreen && <FullscreenOutlined style={{ margin: '0 20px 0 10px' }} onClick={enterFullscreen} />}
          {isFullscreen && <FullscreenExitOutlined style={{ margin: '0 20px 0 10px' }} onClick={exitFullscreen} />}
        </div>
      </nav>
      <div className="monaco-editor-wrapper__editor">
        <Editor
          value={value}
          onChange={onChange}
          language={language}
          defaultValue={defaultValue}
          height={editorHeight}
          theme={theme}
          onMount={handleEditorDidMount}
        />
      </div>
      <div className="footer">
        <div> </div>
        <div className="cursorPosition">
          行 {cursorPosition?.lineNumber || 0},&nbsp; 列 {cursorPosition?.column || 0}{' '}
        </div>
      </div>
    </div>
  );
});
