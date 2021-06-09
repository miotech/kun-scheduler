import React from 'react';
import {
  DisplayParameter,
  DisplayParameterWithSelectType,
  ParameterDisplayType,
} from '@/definitions/TaskTemplate.type';
import { Input, Select } from 'antd';
import { DataSourceSingleSelect } from '@/components/DataSourceSelect';
import { KeyValueTable } from '@/components/KeyValueTable/KeyValueTable';
import StringListInput from '@/components/StringListInput';
import { SparkSQLEditor } from '@/components/CodeEditor';

/**
 * Generate form component by given parameter display type
 * @param parameter
 * @param props
 */
export function formComponentFactory(parameter: DisplayParameter, props: any) {
  const type: ParameterDisplayType | string = parameter.type || '';
  switch (type) {
    case 'sql':
      return <SparkSQLEditor {...props} />;
    case 'string':
      return <Input {...props} />;
    case 'text':
      return <Input.TextArea style={{ minHeight: '140px' }} {...props} />;
    case 'datasource':
      return <DataSourceSingleSelect {...props} />;
    case 'keyvalue':
      return <KeyValueTable {...props} />;
    case 'list':
      return <StringListInput {...props} />;
    case 'single-select':
      return selectFactory(parameter as DisplayParameterWithSelectType, props);
    default:
      return null;
  }
}

function selectFactory(parameter: DisplayParameterWithSelectType, props: any) {
  const options = (parameter.items || []).map(item => (
    <Select.Option value={item.value} key={item.value} title={item.label}>
      {item.label}
    </Select.Option>
  ));
  return <Select {...props}>{options}</Select>;
}
