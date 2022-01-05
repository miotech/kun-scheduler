import React, { useEffect, useState, useCallback, memo } from 'react';
import {DiffEditor} from '@monaco-editor/react';

interface Props {
  original: string,
  modified: string
}
export const EditorDiff: React.FC<Props> = memo(props => {
    const { original,modified } = props
    return (
      
      <DiffEditor
        height="500px"
        language="json"
        original={original}
        modified={modified}
        theme="vs-dark"
      />
    );
  })