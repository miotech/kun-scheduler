import React, { ReactNode } from 'react';
import c from 'classnames';
import { PlayCircleOutlined } from '@ant-design/icons';
import { Controlled as ControlledCodeMirror } from 'react-codemirror2';
import { SQLEditorToolbar } from '@/components/SQLEditor/SQLEditorToolbar';
import { SQLEditorToolButton } from '@/components/SQLEditor/SQLEditorToolButton';
// import { LogUtils } from '@/utils/logUtils';

// codemirror basic styles
import 'codemirror/lib/codemirror.css';
// SQL syntax highlighting
import 'codemirror/mode/sql/sql';
// For code hints
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/addon/hint/show-hint.js';
import 'codemirror/addon/hint/sql-hint.js';

import styles from './SQLEditor.less';

export interface SQLEditorProps {
  // SQL code string
  value: string;
  onChange: (newValue: string) => void;
  theme?: string;
  className?: string;
  children?: ReactNode;
  displayToolbar?: boolean;
}

// const debugLog = LogUtils.getDebugLogger('SQLEditor');

/**
 * SQL code editor for data development
 * @param props
 * @constructor
 */
export const SQLEditor: React.FC<SQLEditorProps> = props => {
  const { value, onChange, theme, displayToolbar = false } = props;

  return (
    <div className={c(styles.container, props.className)}>
      {
        displayToolbar ? (
          <SQLEditorToolbar>
            <SQLEditorToolButton>
              <PlayCircleOutlined />
              <span>试运行 SQL</span>
            </SQLEditorToolButton>
          </SQLEditorToolbar>
        ) : null
      }
      <div className={c(styles.codemirrorWrapper, {
        [styles.WithToolbar]: displayToolbar,
      })}>
        <ControlledCodeMirror
          className={styles.codemirror}
          onBeforeChange={(editor, data, newValue) => {
            onChange(newValue);
          }}
          options={{
            mode: 'sql',
            theme: theme ?? 'default',
            lineNumbers: true,
          }}
          value={value}
          // onCursor={((editor, data) => {
          //   debugLog('editor =%o, data = %o', editor, data);
          // })}
        />
      </div>
    </div>
  );
};
