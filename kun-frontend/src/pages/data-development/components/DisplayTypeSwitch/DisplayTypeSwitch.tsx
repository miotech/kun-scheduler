import React, { memo, useMemo } from 'react';
import c from 'clsx';
import { Tooltip } from 'antd';
import { ApartmentOutlined, UnorderedListOutlined } from '@ant-design/icons';
import { VisuallyHidden } from '@react-aria/visually-hidden';

import styles from './DisplayTypeSwitch.module.less';

interface OwnProps {
  currentType: 'DAG' | 'LIST';
  onChange?: (displayType: 'DAG' | 'LIST') => any;
}

type Props = OwnProps;

const ID_RADIO_DAG = 'id-display-type-switch-DAG';
const ID_RADIO_LIST = 'id-display-type-switch-list';

export const DisplayTypeSwitch: React.FC<Props> = memo(function DisplayTypeSwitch(props) {
  const {
    currentType,
    onChange,
  } = props;

  const ariaElements = useMemo(() => (
    <VisuallyHidden>
      <form>
        <fieldset>
          <legend>Choose display type:</legend>
          <label htmlFor={ID_RADIO_DAG}>
            <input
              id={ID_RADIO_DAG}
              type="radio"
              value="DAG"
              name="DAG"
              checked={currentType === 'DAG'}
              onChange={() => {
                if (onChange) {
                  onChange('DAG');
                }
              }}
            />
            <span>DAG</span>
          </label>
          <label htmlFor={ID_RADIO_LIST}>
            <input
              id={ID_RADIO_LIST}
              type="radio"
              value="LIST"
              name="list"
              checked={currentType === 'LIST'}
              onChange={() => {
                if (onChange) {
                  onChange('DAG');
                }
              }}
            />
            <span>List</span>
          </label>
        </fieldset>
      </form>
    </VisuallyHidden>
  ), [
    currentType,
    onChange,
  ]);

  return (
    <div
      className={styles.DisplayTypeSwitch}
      data-tid="display-type-switch"
    >
      {ariaElements}
      <div
        className={styles.DisplayTypeSwitchContainer}
        data-tid="switchButtonContainer"
      >
        {/* DAG display switch button */}
        <Tooltip title="DAG mode">
          <button
            className={c(styles.DisplayTypeSwitchBtn, {
              [styles.DisplayTypeSwitchBtnActive]: currentType === 'DAG',
            })}
            type="button"
            onClick={() => {
              if (onChange) {
                onChange('DAG');
              }
            }}
            aria-hidden
          >
            <ApartmentOutlined />
          </button>
        </Tooltip>
        {/* List display switch button */}
        <Tooltip title="List mode">
          <button
            className={c(styles.DisplayTypeSwitchBtn, {
              [styles.DisplayTypeSwitchBtnActive]: currentType === 'LIST',
            })}
            type="button"
            onClick={() => {
              if (onChange) {
                onChange('LIST');
              }
            }}
            aria-hidden
          >
            <UnorderedListOutlined />
          </button>
        </Tooltip>
      </div>
    </div>
  );
});
