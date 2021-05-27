import React, { useCallback, useState } from 'react';
import c from 'clsx';
import Icon, { CloseOutlined, ShrinkOutlined, ArrowsAltOutlined, DownOutlined, UpOutlined } from '@ant-design/icons';
import { Button, Tooltip } from 'antd';
import useI18n from '@/hooks/useI18n';

import { ReactComponent as StopIcon } from '@/assets/icons/stop-rect.svg';
import styles from './BottomLayout.less';

export interface BottomLayoutProps {
  visible?: boolean;
  children?: React.ReactNode;
  title?: React.ReactNode;
  onClose?: () => any;
  onStop?: () => any;
  stopBtnDisabled?: boolean;
}

export const BottomLayout: React.FC<BottomLayoutProps> = props => {
  const { visible, children, title = '', onStop, stopBtnDisabled, onClose } = props;
  const t = useI18n();

  const [fullscreen, setFullScreen] = useState<boolean>(false);
  const [shrink, setShrink] = useState<boolean>(false);

  const handleClickFullscreenButton = useCallback(() => {
    setFullScreen(!fullscreen);
  }, [fullscreen]);

  const handleClickShrinkButton = useCallback(() => {
    setShrink(!shrink);
  }, [shrink]);

  return (
    <section
      className={c(styles.BottomLayout, {
        [styles.BottomLayoutVisible]: !!visible,
        [styles.BottomLayoutFullscreen]: !shrink && fullscreen,
        [styles.BottomLayoutShrink]: shrink,
      })}
    >
      <nav className={styles.BottomLayoutNav}>
        <h3 className={styles.BottomLayoutNavTitle}>{title}</h3>
        <div className={styles.BottomLayoutNavButtonGroup}>
          {/* Stop Button */}
          <Tooltip title={t('dataDevelopment.stopDryRun')}>
            <Button
              size="small"
              type="link"
              style={{ marginLeft: '16px' }}
              onClick={onStop}
              disabled={Boolean(stopBtnDisabled)}
              icon={<Icon component={StopIcon} />}
            />
          </Tooltip>
          {/* fullscreen button */}
          <Button
            type="link"
            icon={fullscreen ? <ShrinkOutlined /> : <ArrowsAltOutlined />}
            onClick={handleClickFullscreenButton}
          />
          <Button type="link" icon={shrink ? <UpOutlined /> : <DownOutlined />} onClick={handleClickShrinkButton} />
          {/* Close button */}
          <Button type="link" icon={<CloseOutlined />} onClick={onClose} />
        </div>
      </nav>
      <div className={styles.BottomLayoutContent}>{children}</div>
    </section>
  );
};
