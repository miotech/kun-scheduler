import React, { memo, useCallback, useMemo } from 'react';
import { Button, Col, Input, Row, Table } from 'antd';
import { ColumnProps } from 'antd/es/table';
import useI18n from '@/hooks/useI18n';
import { CloseOutlined, PlusOutlined } from '@ant-design/icons';
import pullAt from 'lodash/pullAt';

export interface KeyValuePair {
  ordinal?: number;
  key: string;
  value: string;
}

interface OwnProps {
  value?: KeyValuePair[];
  onChange?: (pairs: KeyValuePair[]) => any;
  keyTitleText?: React.ReactNode;
  valueTitleText?: React.ReactNode;
}

type Props = OwnProps;

export const KeyValueTable: React.FC<Props> = memo(function KeyValueTable(props) {
  const {
    value,
    keyTitleText,
    valueTitleText,
    onChange,
  } = props;

  const t = useI18n();

  const columns: ColumnProps<KeyValuePair>[] = useMemo(() => [
    {
      title: keyTitleText ?? t('common.key'),
      key: 'key',
      render: (txt, record, index) => {
        return (
          <Input
            className="full-width"
            size="small"
            value={record.key}
            onChange={(ev) => {
              if (onChange && value) {
                const nextValueState = [...value];
                nextValueState[index] = {
                  ordinal: nextValueState[index].ordinal,
                  key: ev.target.value,
                  value: nextValueState[index].value,
                };
                onChange(nextValueState);
              }
            }}
          />
        );
      },
    },
    {
      title: valueTitleText ?? t('common.value'),
      key: 'value',
      render: (txt, record, index) => {
        return (
          <Input
            className="full-width"
            size="small"
            value={record.value}
            onChange={(ev) => {
              if (onChange && value) {
                const nextValueState = [...value];
                nextValueState[index] = {
                  ordinal: nextValueState[index].ordinal,
                  key: nextValueState[index].key,
                  value: ev.target.value,
                };
                onChange(nextValueState);
              }
            }}
          />
        );
      },
    },
    {
      title: '',
      key: 'operations',
      width: 60,
      render: (txt, record, index) => {
        return (
          <Button
            type="link"
            size="small"
            onClick={() => {
              if (onChange && value) {
                let nextValueState = [...value];
                pullAt(nextValueState, index);
                nextValueState = nextValueState.map(r => ({ ...r, ordinal: index }));
                onChange(nextValueState);
              }
            }}
          >
            <CloseOutlined />
          </Button>
        );
      },
    },
  ], [
    keyTitleText,
    valueTitleText,
    onChange,
    value,
    t,
  ]);

  const addKeyValuePair = useCallback(function addKeyValuePair() {
    if (value && onChange) {
      onChange([...value, {
        ordinal: value.length,
        key: '',
        value: '',
      }]);
    }
  }, [
    onChange,
    value,
  ]);

  const footerRender = () => (
    <Row>
      <Col flex="1 1">
        <Button
          type="dashed"
          onClick={addKeyValuePair}
          style={{ width: '100%' }}
        >
          <PlusOutlined />
          <span>{t('common.button.add')}</span>
        </Button>
      </Col>
    </Row>
  );

  return (
    <div style={{ background: '#fff' }}>
      <Table<KeyValuePair>
        bordered
        size="small"
        columns={columns}
        dataSource={value || []}
        footer={footerRender}
        pagination={false}
        rowKey={r => `${r.ordinal}::${r.key}::${r.value}`}
      />
    </div>
  );
});
