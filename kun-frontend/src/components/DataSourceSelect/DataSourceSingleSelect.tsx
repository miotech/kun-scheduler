import React, {
  FunctionComponent,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import c from 'clsx';
import { useRequest } from 'ahooks';
import { searchDataBasesService } from '@/services/dataSettings';
import { Select } from 'antd';
import { KunSpin } from '@/components/KunSpin';

interface OwnProps {
  value?: string;
  onChange?: (dataSrcId: string) => any;
  className?: string;
}

type Props = OwnProps;

export const DataSourceSingleSelect: FunctionComponent<Props> = props => {
  const { value, onChange, className } = props;
  const [searchName, setSearchName] = useState<string>('');

  const { data, loading, run: doSearch } = useRequest(searchDataBasesService, {
    throttleInterval: 1000,
    manual: true,
  });

  useEffect(() => {
    doSearch(searchName || '', {
      pageNumber: 1,
      pageSize: 50,
    });
  }, [doSearch, searchName]);

  const options = useMemo(() => {
    if (!data) {
      return [];
    }
    // else
    return data.datasources.map(db => (
      <Select.Option key={db.id} value={db.id}>
        {db.name}
      </Select.Option>
    ));
  }, [data]);

  const handleSearch = useCallback(
    (searchText: string) => {
      setSearchName(searchText);
    },
    [setSearchName],
  );

  return (
    <Select
      showSearch
      className={c('datasource-single-select', className)}
      notFoundContent={loading ? <KunSpin size="small" /> : null}
      value={value}
      onChange={onChange}
      onSearch={handleSearch}
      filterOption={false}
    >
      {options}
    </Select>
  );
};
