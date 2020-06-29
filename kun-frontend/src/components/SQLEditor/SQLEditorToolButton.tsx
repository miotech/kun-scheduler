import React from 'react';

import styles from './SQLEditorToolButton.less';

export const SQLEditorToolButton: React.FC<any> = props => {

  return (
    <button
      type="button"
      className={styles.toolButton}
    >
      {props.children}
    </button>
  );
};
