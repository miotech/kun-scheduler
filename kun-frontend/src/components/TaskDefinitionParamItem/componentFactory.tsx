import React from 'react';
import { ParameterDisplayType } from '@/definitions/TaskTemplate.type';
import { SQLEditor } from '@/components/SQLEditor';
import { Input } from 'antd';
import { DataSourceSingleSelect } from '@/components/DataSourceSelect';
import { KeyValueField } from '@/components/KeyValueField';

/**
 * Generate form component by given parameter display type
 * @param type
 * @param props
 */
export function formComponentFactory(type: ParameterDisplayType | string, props: any) {
  switch (type) {
    case 'sql':
      return (
        <SQLEditor
          {...props}
        />
      );
    case 'string':
      return (
        <Input
          {...props}
        />
      );
    case 'text':
      return (
        <Input.TextArea
          {...props}
        />
      );
    case 'datasource':
      return (
        <DataSourceSingleSelect
          {...props}
        />
      );
    case 'keyvalue':
      return <KeyValueField
        {...props}
      />;
    default:
      return null;
  }
}
