import React from 'react';
import { mount } from 'enzyme';
import renderer from 'react-test-renderer';

import { KunSpin } from '../KunSpin';

test('renders correctly',  () => {
  const component = renderer.create(<KunSpin />).toJSON();
  expect(component).toMatchSnapshot();
});

test('mounts correctly', async () => {
  expect(() => {
    mount(<KunSpin />);
  }).not.toThrow();
});
