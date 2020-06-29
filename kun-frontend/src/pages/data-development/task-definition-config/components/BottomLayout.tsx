import React, { useCallback, useState } from 'react';
import c from 'classnames';
import { CloseOutlined, ShrinkOutlined, ArrowsAltOutlined } from '@ant-design/icons';
import { Button } from 'antd';

import styles from './BottomLayout.less';

export interface BottomLayoutProps {
  visible?: boolean;
  children?: React.ReactNode;
  title?: React.ReactNode;
  onClose?: () => any;
}

export const BottomLayout: React.FC<BottomLayoutProps> = props => {
  const {
    visible,
    children,
    title = '',
    onClose,
  } = props;

  const [ shrink, setShrink ] = useState<boolean>(false);

  const handleClickShrinkButton = useCallback(() => {
    setShrink(!shrink);
  }, [
    shrink,
    setShrink,
  ]);

  return (
    <section className={c(styles.BottomLayout, {
      [styles.BottomLayoutVisible]: !!visible,
      [styles.BottomLayoutShrink]: shrink,
    })}>
      <nav className={styles.BottomLayoutNav}>
        <h3 className={styles.BottomLayoutNavTitle}>{title}</h3>
        <div className={styles.BottomLayoutNavButtonGroup}>
          {/* Shrink button */}
          <Button
            type="link"
            icon={shrink ? <ArrowsAltOutlined /> : <ShrinkOutlined />}
            onClick={handleClickShrinkButton}
          />
          {/* Close button */}
          <Button
            type="link"
            icon={<CloseOutlined />}
            onClick={onClose}
          />
        </div>
      </nav>
      <div className={styles.BottomLayoutContent}>
        {children}
      </div>
    </section>
  );
};
