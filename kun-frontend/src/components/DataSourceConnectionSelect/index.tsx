import React, { FunctionComponent, useEffect, useMemo } from 'react';
import c from 'clsx';
import { useRequest } from 'ahooks';
import { getUserConnection } from '@/services/dataSettings';
import { Select, Form } from 'antd';
import { KunSpin } from '@/components/KunSpin';
import { FormInstance } from 'antd/es/form';
import { UserConnection } from '@/definitions/DataSource.type';
import { RootState } from '@/rematch/store';
import useRedux from '@/hooks/useRedux';
import { UserState } from '@/rematch/models/user';

interface OwnProps {
  value?: string;
  onChange?: (dataSrcId: string) => any;
  className?: string;
  form: FormInstance;
  name: string;
}

type Props = OwnProps;

export const DataSourceConnectionSelect: FunctionComponent<Props> = props => {
  const { value, onChange, className, form, name } = props;

  const { selector } = useRedux<UserState>((state: RootState) => state.user);

  const { data, loading, run: doSearch } = useRequest(getUserConnection, {
    debounceWait: 1000,
    manual: true,
  });
  const sourceDataSource = Form.useWatch(['taskPayload', 'taskConfig', 'sourceDataSource'], form);
  const targetDataSource = Form.useWatch(['taskPayload', 'taskConfig', 'targetDataSource'], form);
  useEffect(() => {
    if (name === 'sourceDataSourceConnection' && sourceDataSource) {
      doSearch(sourceDataSource);
    }
  }, [sourceDataSource, name, doSearch]);

  useEffect(() => {
    if (name === 'targetDataSourceConnection' && targetDataSource) {
      doSearch(targetDataSource);
    }
  }, [targetDataSource, name, doSearch]);

  const options = useMemo(() => {
    if (!data) {
      return [];
    }
    // else
    return data?.map((db: UserConnection) => (
      <Select.Option
        key={db.id}
        value={db.id}
        disabled={db.securityUserList.length && !db.securityUserList.includes(selector.username)}
      >
        {db.name}
      </Select.Option>
    ));
  }, [data, selector.username]);

  return (
    <Select
      showSearch
      className={c('datasource-connect-select', className)}
      notFoundContent={loading ? <KunSpin size="small" /> : null}
      value={value}
      onChange={onChange}
      filterOption={false}
    >
      {options}
    </Select>
  );
};
