import React from 'react';
import { Meta } from '@storybook/react';
import { IntlProvider } from '@@/core/umiExports';

import { DatasetNodeCard } from './DatasetNodeCard';

export default {
  title: 'components/LineageDiagram/DatasetNodeCard',
  component: DatasetNodeCard,
} as Meta;

const demoMessage = {
  'lineageDiagram.nodeCard.rowCount': 'Row Count'
};

export const DatasetNodeCardDemo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <DatasetNodeCard
        state="default"
        // @ts-ignore
        data={{
          id: '123',
          name: 'miocompany',
        }}
        rowCount={1000000}
        lastUpdateTime={Date.now()}
        useNativeLink
      />
    </IntlProvider>
  );
};

export const DatasetNodeCardWithPorts = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <DatasetNodeCard
        state="default"
        // @ts-ignore
        data={{
          id: '123',
          name: 'miocompany',
        }}
        rowCount={1000000}
        lastUpdateTime={Date.now()}
        useNativeLink
        leftPortState="collapsed"
        rightPortState="expanded"
      />
    </IntlProvider>
  );
};
