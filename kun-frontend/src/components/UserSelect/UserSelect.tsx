import React, { FunctionComponent, useMemo } from 'react';
import { SelectProps } from 'antd/es/select';
import { Select } from 'antd';
import { useRequest } from 'ahooks';
import { fetchUsersList } from '@/services/user';
import { KunSpin } from '@/components/KunSpin';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  value?: string | string[];
  onChange?: (nextUserValue: string | string[]) => any;
}

type Props = SelectProps<string | string[]> & OwnProps;

export const UserSelect: FunctionComponent<Props> = (props) => {
  const { value, onChange, placeholder, ...restProps } = props;

  const t = useI18n();

  const {
    data: respData,
    loading,
  } = useRequest(fetchUsersList, {
    throttleInterval: 500,
    // cache up user list for later use
    cacheKey: 'fetchUsersList',
  });

  const users = respData || [];

  const options = useMemo(() => users.map(user => (
    <Select.Option key={user.id} value={user.id} label={user.username}>
      {user.username}
    </Select.Option>
  )), [
    users,
  ]);

  return (
    <Select
      disabled={props.disabled || (!users.length)}
      showSearch
      optionFilterProp="label"
      notFoundContent={loading ? <KunSpin size="small" /> : null}
      {...restProps}
      // if user list is still loading, disable selector
      placeholder={loading ? `${t('common.loading')}` : placeholder}
      value={(!users.length) ? undefined : value}
      onChange={onChange}
    >
      {options}
    </Select>
  );
};
