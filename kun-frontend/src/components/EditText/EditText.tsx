import React from 'react';
import EdiText, { EdiTextProps } from 'react-editext';

import './EditText.less';

export interface EditTextProps extends EdiTextProps {
  onChange?: (value: string) => any;
}

export const EditText: React.FC<EditTextProps> = props => {
  const {
    ...restProps
  } = props;

  return <EdiText
    type="text"
    onSave={(value, inputProps) => {
      if (props.onChange) {
        props.onChange(value);
      }
      props.onSave(value, inputProps);
    }}
    {...restProps}
  />;
};
