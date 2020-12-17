import React, { useState } from 'react';
import { Meta } from '@storybook/react';
import { IntlProvider } from '@@/core/umiExports';
import { DisplayTypeSwitch } from '@/pages/data-development/components/DisplayTypeSwitch/DisplayTypeSwitch';

import '@/global.less';

export default {
  title: 'components/DataDevelopment/DisplayTypeSwitch',
  component: DisplayTypeSwitch,
} as Meta;

const demoMessage = {
};

export const DisplayTypeSwitchDemo = () => {
  const [ value, setValue ] = useState<'DAG' | 'LIST'>('DAG');

  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main>
        <DisplayTypeSwitch
          currentType={value}
          onChange={setValue}
        />
      </main>
    </IntlProvider>
  );
};
