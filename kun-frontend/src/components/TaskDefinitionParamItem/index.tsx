import React from 'react';
import { Form } from 'antd';
import { DisplayParameter } from '@/definitions/TaskTemplate.type';
import { FormItemProps } from 'antd/es/form';
import { formComponentFactory } from '@/components/TaskDefinitionParamItem/componentFactory';

interface TaskDefinitionParamItemProps {
  parameter?: DisplayParameter | null;
  wrapFormItem?: FormItemProps | boolean;
  value?: any;
  onChange?: any;
  className?: string;
  style?: React.CSSProperties;
}


export const TaskDefinitionParamItem: React.FC<TaskDefinitionParamItemProps> = props => {
  const { parameter, wrapFormItem, ...restProps } = props;

  if (!parameter) {
    return <></>;
  }

  const matchedFormComponent = formComponentFactory(parameter.type, restProps);

  if (!wrapFormItem) {
    return matchedFormComponent;
  }

  const formItemProps = (typeof wrapFormItem === 'boolean') ? {} : wrapFormItem;
  // else
  return (
    <Form.Item
      name={parameter?.name}
      rules={[{ required: parameter?.required || false }]}
      label={parameter?.displayName}
      {...formItemProps}
    >
      {matchedFormComponent}
    </Form.Item>
  );
};
