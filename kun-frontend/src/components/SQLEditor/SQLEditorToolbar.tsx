import React from 'react';
import styles from './SQLEditorToolbar.less';

export interface SQLEditorToolbarProps extends React.ComponentProps<any> {
}

export const SQLEditorToolbar: React.FC<SQLEditorToolbarProps> = props => {
  return (
    <div className={styles.toolbar}>
      {props.children}
    </div>
  );
};
