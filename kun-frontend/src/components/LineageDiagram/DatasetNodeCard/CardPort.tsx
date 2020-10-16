import React, { memo } from 'react';
import c from 'clsx';

export type PortTypes = 'hidden' | 'collapsed' | 'expanded' | 'loading';

export type OnExpandEventHandler = (ev: React.MouseEvent<any>) => any;

export type OnCollapseEventHandler = (ev: React.MouseEvent<any>) => any;

interface OwnProps {
  className?: string;
  portState: PortTypes;
  onExpand?: OnExpandEventHandler;
  onCollapse?: OnCollapseEventHandler;
}

type Props = OwnProps;

const clsPrefix = 'lineage-dataset-node-card';

export const CardPort: React.FC<Props> = memo(function CardPort(props) {
  const { portState, onExpand, onCollapse, className } = props;

  switch (portState) {
    case 'collapsed':
      return (
        <button
          type="button"
          className={c(
            `${clsPrefix}__port-btn`,
            `${clsPrefix}__port-btn--collapsed`,
            className,
          )}
          aria-label="port-collapsed"
          onClick={onExpand}
        >
          <span>+</span>
        </button>
      );
    case 'expanded':
      return (
        <button
          type="button"
          className={c(
            `${clsPrefix}__port-btn`,
            `${clsPrefix}__port-btn--expanded`,
            className,
          )}
          aria-label="port-expanded"
          onClick={onCollapse}
        >
          <span>-</span>
        </button>
      );
    case 'loading':
      return (
        <button
          type="button"
          className={c(
            `${clsPrefix}__port-btn`,
            `${clsPrefix}__port-btn--loading`,
            className,
          )}
          aria-label="port-loading"
          disabled
        >
          ...
        </button>
      );
    case 'hidden':
    default:
      return <></>;
  }
});
