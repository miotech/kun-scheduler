import React, {
  memo,
  useCallback,
  RefForwardingComponent,
  forwardRef,
} from 'react';
import c from 'clsx';
import Iconfont from '../Iconfont';
import styles from './IconButton.less';

export interface Props {
  iconClassName?: string;
  className?: string;
  icon: string;
  onClick?: () => void;
  disabled?: boolean;
}

const IconButton: RefForwardingComponent<HTMLDivElement, Props> = (
  {
    iconClassName,
    className,
    icon,
    onClick = () => {},
    disabled = false,
    ...rest
  }: Props,
  ref,
) => {
  const handleClick = useCallback(() => {
    if (!disabled) {
      onClick();
    }
  }, [disabled, onClick]);

  return (
    <div
      {...rest}
      ref={ref}
      className={c(styles.iconButton, className, {
        [styles.disabled]: disabled,
      })}
      onClick={handleClick}
    >
      <Iconfont className={iconClassName} type={icon} />
    </div>
  );
};

export default memo(forwardRef(IconButton));
