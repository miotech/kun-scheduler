import React from 'react';
import { Meta } from '@storybook/react/types-6-0';
import { KunLoading } from './KunLoading';

import './KunLoading.global.less';

export default {
  title: 'Components/KunLoading',
  component: KunLoading,
} as Meta;

export const LoadingAnimation = () => (
  <KunLoading />
);
