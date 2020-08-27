import React, { RefObject } from 'react';
import { Alert } from 'antd';
import sinon from 'sinon';
import { mount, shallow } from 'enzyme';
import { mocked } from 'ts-jest/utils';

import cronstrue from 'cronstrue/i18n';
import useI18n from '@/hooks/useI18n';

import { CronExpressionInput } from '../CronExpressionInput';

// mock useI18n hook
jest.mock('@/hooks/useI18n');
const mockUseI18n = mocked(useI18n, true);

describe('component CronExpressionInput', () => {

  beforeAll(() => {
    mockUseI18n.mockReturnValue((txtId) => {
      if (txtId === 'common.cronstrue.lang') {
        return 'en';
      }
      // else
      return txtId;
    });
  });

  it('should renders properly', () => {
    expect(() => {
      mount(<CronExpressionInput />);
    }).not.toThrow();
  });

  it('should forward focus event to its inner input', () => {
    const ref = React.createRef() as RefObject<HTMLInputElement>;
    const wrapper = mount(<CronExpressionInput ref={ref} />);
    ref.current?.focus();

    const inputElement = wrapper.find('input').getDOMNode();
    const focusedElement = document.activeElement;
    expect(inputElement).toBe(focusedElement);
  });

  it('should renders semantic tip when passed a valid cron expression value', () => {
    const wrapper = shallow(<CronExpressionInput value="0 8 * * * ?" />);
    expect(wrapper.find(Alert)).toHaveLength(1);
    expect(wrapper.find(Alert).props().type).toBe('success');
    expect(wrapper.find(Alert).props().message).toBe(cronstrue.toString('0 8 * * * ?'));
  });

  it('should renders error alert when passed an invalid cron expression value', () => {
    const wrapper = shallow(<CronExpressionInput value="1231234" />);
    expect(wrapper.find(Alert)).toHaveLength(1);
    expect(wrapper.find(Alert).props().type).toBe('error');
  });

  it('should hide error alert when "hideErrorAlert" property is assigned', () => {
    const wrapper = shallow(<CronExpressionInput value="1231234" hideErrorAlert />);
    expect(wrapper.find(Alert)).toHaveLength(0);
  });

  it('should reacts to user input change event dynamically', () => {
    const onChange = sinon.spy();
    const wrapper = mount(<CronExpressionInput value="" onChange={onChange} />);
    wrapper.find('input').simulate('change', { target: { value: '0 8 * * * ?' } });
    expect(onChange.callCount).toBe(1);
    expect(onChange.args[0][0]).toEqual('0 8 * * * ?');
  });
});
