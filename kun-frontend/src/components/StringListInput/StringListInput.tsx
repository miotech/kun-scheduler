import React, { useEffect, useState } from 'react';
import cloneDeep from 'lodash/cloneDeep';
import pullAt from 'lodash/pullAt';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Input, Table } from 'antd';
import { ColumnProps } from 'antd/es/table';
import produce from 'immer';

export interface StringListInputProps {
  title?: string;
  defaultValue?: string[];
  value?: string[] | string;
  onChange?: (newStringArr: string[]) => any;
  autoFillEmptyArray?: boolean;
}

interface InternalData {
  value: string;
  key: string;
}

function parseValue(value: string | string[]): string[] {
  if (typeof value === 'string') {
    return JSON.parse(value) as string[];
  }
  // else
  return value;
}

const StringListInput: React.FC<StringListInputProps> = function StringListInput(props) {
  const [valuesState, setValues] = useState<string[]>(cloneDeep(props?.defaultValue) || []);
  const values = props.value ? parseValue(props.value) : valuesState;

  useEffect(() => {
    if (props.autoFillEmptyArray) {
      if (props.onChange && !props.value) {
        props.onChange([]);
      } else if (!values) {
        setValues([]);
      }
    }
  }, [props.value, props.onChange, props.autoFillEmptyArray, values]);

  const tblData: InternalData[] = values.map((s, idx) => ({
    value: s,
    key: `${idx}-s`,
  }));

  const setStringValueByIndex = (newValue: string, idx: number): void => {
    const newValues = produce(values, draft => {
      draft[idx] = newValue;
    });
    if (props.onChange) {
      props.onChange(newValues);
    } else {
      setValues(newValues);
    }
  };

  const removeStringItemByIndex = (idx: number): void => {
    if (idx < 0 || idx > values.length) {
      return undefined;
    }
    // else
    const newValues = cloneDeep(values);
    pullAt(newValues, [idx]);
    if (props.onChange) {
      props.onChange(newValues);
    } else {
      setValues(newValues);
    }
    return undefined;
  };

  const addStringItem = () => {
    const newValues = (cloneDeep(values) || []).concat(['']);
    if (props.onChange) {
      props.onChange(newValues);
    } else {
      setValues(newValues);
    }
  };

  const columns: ColumnProps<InternalData>[] = [
    {
      title: ' String值',
      render: (txt, record, idx) => (
        <Input size="small" value={values[idx]} onChange={ev => setStringValueByIndex(ev.target.value, idx)} />
      ),
      key: 'value',
    },
    {
      title: '操作',
      width: 60,
      render: (txt, record, idx) => (
        <span>
          <Button
            icon={<DeleteOutlined />}
            danger
            onClick={() => {
              removeStringItemByIndex(idx);
            }}
          />
        </span>
      ),
    },
  ];

  const tblFooter = (
    <div style={{ textAlign: 'center' }}>
      <Button
        size="small"
        icon={<PlusOutlined />}
        onClick={() => {
          addStringItem();
        }}
      >
        添加项目
      </Button>
    </div>
  );

  return (
    <div>
      <Table
        showHeader={false}
        size="small"
        bordered
        columns={columns}
        dataSource={tblData}
        pagination={false}
        rowKey={record => record.key}
        footer={() => tblFooter}
      />
    </div>
  );
};

export default StringListInput;
