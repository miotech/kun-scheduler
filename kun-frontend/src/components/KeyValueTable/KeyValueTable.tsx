import React, { memo, useCallback, useMemo } from 'react';
import { Button, Col, Input, Row, Table, message } from 'antd';
import { ColumnProps } from 'antd/es/table';
import useI18n from '@/hooks/useI18n';
import { CloseOutlined, PlusOutlined } from '@ant-design/icons';
import pullAt from 'lodash/pullAt';
import styles from './KeyValueTable.less';

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
  const { value, keyTitleText, valueTitleText, onChange } = props;

  const t = useI18n();
  const columns: ColumnProps<KeyValuePair>[] = useMemo(
    () => [
      {
        title: keyTitleText ?? t('common.key'),
        key: 'key',
        render: (txt, record, index) => {
          return (
            <Input
              className="full-width"
              size="small"
              value={record.key}
              onChange={ev => {
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
              onChange={ev => {
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
    ],
    [keyTitleText, valueTitleText, onChange, value, t],
  );

  const addKeyValuePair = useCallback(
    function addKeyValuePair() {
      if (value && onChange) {
        onChange([
          ...value,
          {
            ordinal: value.length,
            key: '',
            value: '',
          },
        ]);
      } else if (value == null && onChange) {
        onChange([
          {
            ordinal: 0,
            key: '',
            value: '',
          },
        ]);
      }
    },
    [onChange, value],
  );

  const footerRender = () => (
    <Row>
      <Col flex="1 1">
        <Button type="dashed" onClick={addKeyValuePair} style={{ width: '100%' }}>
          <PlusOutlined />
          <span>{t('common.button.add')}</span>
        </Button>
      </Col>
    </Row>
  );

  const Copy = useCallback(() => {
    navigator.clipboard.writeText(JSON.stringify(value));
    message.success(t('common.reactlazylog.copyToClipboardSuccess'));
  }, [value, t]);

  const Paste = useCallback(() => {
    navigator.clipboard
      .readText()
      .then(text => {
        const parsed = JSON.parse(text);
        if (Array.isArray(parsed)) {
          if (onChange) {
            onChange(parsed);
          }
        } else {
          message.error('Invalid JSON');
        }
      })
      .catch(() => {
        message.error('Invalid JSON');
      });
  }, [onChange]);

  return (
    <div>
      <div className={styles.copy} onClick={Copy}>
        {t('common.button.copy')}
      </div>
      <Table<KeyValuePair>
        style={{ background: '#fff' }}
        bordered
        size="small"
        columns={columns}
        dataSource={value || []}
        footer={footerRender}
        pagination={false}
        rowKey={r => `${r.ordinal}`}
      />
      <div onClick={Paste} className={styles.paste}>
        {t('common.button.paste')}
      </div>
    </div>
  );
});
