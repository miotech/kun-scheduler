import React from 'react';
import { Meta } from '@storybook/react/types-6-0';
import Icon from '@ant-design/icons';

import { ReactComponent as SparkIcon } from '@/assets/icons/apachespark.svg';
import { ReactComponent as SqlIcon } from '@/assets/icons/node-SQL.svg';

export default {
  title: 'Components/Icon',
  component: Icon,
} as Meta;

export const IconList = () => (
  <ul>
    <li>
      <dl>
        <dt>
          <Icon component={SparkIcon} style={{ fontSize: '1.5em', color: 'orange' }} />
        </dt>
        <dd>Spark</dd>
      </dl>
    </li>
    <li>
      <dl>
        <dt><Icon component={SqlIcon} style={{ fontSize: '1.5em' }} /></dt>
        <dd>SQL</dd>
      </dl>
    </li>
  </ul>
);
