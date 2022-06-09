import React, { memo } from 'react';
import c from 'clsx';
import { ApartmentOutlined, UnorderedListOutlined } from '@ant-design/icons';
import { GlossaryDisplayType } from '@/definitions/Glossary.type';

import styles from './index.less';

interface OwnProps {
  currentType: GlossaryDisplayType;
  onChange?: (displayType: GlossaryDisplayType) => any;
}

type Props = OwnProps;

export const DisplayTypeSwitch: React.FC<Props> = memo(function DisplayTypeSwitch(props) {
  const { currentType, onChange } = props;

  return (
    <div className={styles.DisplayTypeSwitch} data-tid="display-type-switch">
      <div className={styles.DisplayTypeSwitchContainer} data-tid="switchButtonContainer">
        {/* RELATION display switch button */}
        <button
          className={c(styles.DisplayTypeSwitchBtn, {
            [styles.DisplayTypeSwitchBtnActive]: currentType === GlossaryDisplayType.RELATION,
          })}
          type="button"
          onClick={() => {
            if (onChange) {
              onChange(GlossaryDisplayType.RELATION);
            }
          }}
          aria-hidden
        >
          <ApartmentOutlined />
        </button>
        {/* List display switch button */}
        <button
          className={c(styles.DisplayTypeSwitchBtn, {
            [styles.DisplayTypeSwitchBtnActive]: currentType === GlossaryDisplayType.LIST,
          })}
          type="button"
          onClick={() => {
            if (onChange) {
              onChange(GlossaryDisplayType.LIST);
            }
          }}
          aria-hidden
        >
          <UnorderedListOutlined />
        </button>
      </div>
    </div>
  );
});
