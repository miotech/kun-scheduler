import React, { FunctionComponent, ReactNode, useMemo, useState } from 'react';
import { Button, Col, Input, List, message, Popover, Row, Typography } from 'antd';
import map from 'lodash/map';
import omit from 'lodash/omit';
import has from 'lodash/has';
import { CloseOutlined, PlusOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  value?: Record<string, string>;
  onChange?: (nextValue?: Record<string, string>) => any;
  nullWhenEmpty?: boolean;
  keyTitleText?: string | ReactNode;
  valueTitleText?: string | ReactNode;
}

type Props = OwnProps;

import './KeyValueField.less';

export const KeyValueField: FunctionComponent<Props> = (props) => {
  const {
    value: propsValue,
    onChange,
    keyTitleText,
    valueTitleText,
  } = props;

  const t = useI18n();

  const [ newPairKeyStr, setNewPairKeyStr ] = useState<string>('')
  const [ newPairValueStr, setNewPairValueStr ] = useState<string>('');

  const renderKeyValuePairRow = (key: string, val: string) => {
    if (!propsValue) {
      return <></>;
    }
    return (
      <Row key={`${key}-${propsValue}`} data-tid="key-value-pair-row">
        {/* Key */}
        <Col flex="1 1">
          <Typography.Text editable={{
            onChange: (nextKeyStr: string) => {
              const hasConflict: boolean = has(omit(propsValue, [key]) || {}, nextKeyStr);
              if (hasConflict) {
                return message.error(t('common.keyvalue.keyNameConflict', {
                  keyName: nextKeyStr,
                }));
              }
              // else
              onChange && onChange({
                ...omit(propsValue, [key]),
                [nextKeyStr]: propsValue[key],
              });
            },
          }}>
            {key}
          </Typography.Text>
        </Col>
        {/* Value */}
        <Col flex="1 1">
          <Typography.Text editable={{
            onChange: (nextValueStr: string) => {
              onChange && onChange({
                ...propsValue,
                [key]: nextValueStr,
              });
            },
          }}>
            {val}
          </Typography.Text>
        </Col>
        {/* Operations */}
        <Col flex="60px">
          <Button
            type="link"
            size="small"
            onClick={() => {
              onChange && onChange(omit(propsValue, key));
            }}
          >
            <CloseOutlined />
          </Button>
        </Col>
      </Row>
    );
  };

  const header = (
    <Row data-tid="key-value-pair-header">
      {/* Key */}
      <Col flex="1 1">
        <em>{keyTitleText ?? t('common.key')}</em>
      </Col>
      {/* Value */}
      <Col flex="1 1">
        <em>{valueTitleText ?? t('common.value')}</em>
      </Col>
      {/* operations */}
      <Col flex="60px" />
    </Row>
  );

  const addKeyValuePairForm = useMemo(() => {
    return (
      <div style={{ width: '400px' }}>
        <Row>
          <Col flex="0 0 50px">
            {keyTitleText ?? t('common.key')}:
          </Col>
          <Col flex="1 1">
            <Input
              size="small"
              value={newPairKeyStr}
              onChange={ev => setNewPairKeyStr(ev.target.value)}
            />
          </Col>
        </Row>
        <Row style={{ marginTop: '8px' }}>
          <Col flex="0 0 50px">
            {keyTitleText ?? t('common.value')}:
          </Col>
          <Col flex="1 1">
            <Input
              size="small"
              value={newPairValueStr}
              onChange={ev => setNewPairValueStr(ev.target.value)}
            />
          </Col>
        </Row>
        <Row style={{ marginTop: '8px' }}>
          <Button
            type="primary" size="small"
            style={{ width: '100%' }}
            onClick={() => {
              if (has(propsValue, newPairValueStr)) {
                return message.error(t('common.keyvalue.keyNameConflict', {
                  keyName: newPairValueStr,
                }));
              }
              // else
              onChange && onChange({
                ...propsValue,
                [newPairKeyStr]: newPairValueStr,
              });
              setNewPairKeyStr('');
              setNewPairValueStr('');
            }}
          >
            {t('common.button.add')}
          </Button>
        </Row>
      </div>
    );
  }, [
    newPairKeyStr,
    newPairValueStr,
    setNewPairKeyStr,
    setNewPairValueStr,
  ]);

  const footer = (
    <Row>
      <Col flex="1 1">
        <Popover
          trigger="click"
          content={addKeyValuePairForm}
          title={t('common.addKeyValuePair')}
        >
          <Button
            type="dashed"
            onClick={() => {}}
            style={{ width: '100%' }}
          >
            <PlusOutlined />
            <span>{t('common.button.add')}</span>
          </Button>
        </Popover>
      </Col>
    </Row>
  );

  return (
    <div className="key-value-field">
      <List
        bordered
        header={header}
        dataSource={map((propsValue || {}), (v, k) => {
          return renderKeyValuePairRow(k, v);
        })}
        renderItem={item => item}
        footer={footer}
      />
    </div>
  );
};
