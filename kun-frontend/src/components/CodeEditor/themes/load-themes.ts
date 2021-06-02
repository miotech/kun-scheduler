/* eslint-disable no-underscore-dangle */
import { Monaco } from '@monaco-editor/react';
import ThemeMonokai from './monokai.theme';
import ThemeXCode from './xcode.theme';
import ThemeGitHub from './github.theme';
import ThemeBlackboard from './blackboard.theme';


export default function loadMonacoThemes(monaco: Monaco) {
  // @ts-ignore
  if (!window.__MONACO_EDITOR_THEME_DEFINED__) {
    monaco.editor.defineTheme('monokai', ThemeMonokai as any);
    monaco.editor.defineTheme('xcode', ThemeXCode as any);
    monaco.editor.defineTheme('github', ThemeGitHub as any);
    monaco.editor.defineTheme('blackboard', ThemeBlackboard as any);
    // @ts-ignore
    window.__MONACO_EDITOR_THEME_DEFINED__ = true;
  }
}
