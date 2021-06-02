/* eslint-disable no-underscore-dangle */
import { Monaco } from '@monaco-editor/react';
import ThemeLightPlus from './light-plus.theme';
import ThemeMonakai from './monakai.theme';

export default function loadMonacoThemes(monaco: Monaco) {
  // @ts-ignore
  if (!window.__MONACO_EDITOR_THEME_DEFINED__) {
    monaco.editor.defineTheme('monakai', ThemeMonakai as any);
    monaco.editor.defineTheme('monakai', ThemeLightPlus as any);
    // @ts-ignore
    window.__MONACO_EDITOR_THEME_DEFINED__ = true;
  }
}
