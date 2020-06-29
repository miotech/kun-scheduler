import React from 'react';
import { mount } from 'enzyme';
import renderer from 'react-test-renderer';

import { KunLoading } from '../KunLoading';

test('renders correctly',  () => {
  const component = renderer.create(<KunLoading />).toJSON();
  expect(component).toMatchSnapshot();
});

test('mounts correctly', async () => {
  expect(() => {
    mount(<KunLoading />);
  }).not.toThrow();
});
