import React, {
  memo,
  forwardRef,
  useImperativeHandle,
  RefForwardingComponent,
  useState,
  useCallback,
  useEffect,
} from 'react';
import { Input, Select } from 'antd';
import c from 'clsx';
import lowerCase from 'lodash/lowerCase';
import upperFirst from 'lodash/upperFirst';

import useI18n from '@/hooks/useI18n';

import {
  ValidateRuleItem,
  ValidateOperatorEnum,
  ValidateFieldType,
  validateOperatorEnumToLocaleString,
} from '@/rematch/models/dataQuality';

import styles from './ValidateRule.less';

const { Option } = Select;

export interface GetValueResult {
  value: ValidateRuleItem;
  key: string;
}

export interface ValidateRuleHandle {
  getValue: () => GetValueResult | null;
  key: string;
}

interface Props {
  ruleKey: string;
  dimension: string | null;
  defaultRule?: ValidateRuleItem;
  setIsUpdatedRule: () => void;
  // defaultSelectedField?: string;
}

const ValidateRule: RefForwardingComponent<ValidateRuleHandle, Props> = (
  { ruleKey, defaultRule, dimension, setIsUpdatedRule }: Props,
  ref,
) => {
  const t = useI18n();

  const [fieldName, setFieldName] = useState('');
  const [operator, setOperator] = useState<ValidateOperatorEnum | undefined>(undefined);
  const [fieldType, setFieldType] = useState<ValidateFieldType>(ValidateFieldType.BOOLEAN);
  const [fieldValue, setFieldValue] = useState<string | undefined>('true');

  useEffect(() => {
    if (defaultRule) {
      setFieldName(defaultRule.field || '');
      setOperator(defaultRule.operator);
      setFieldType(defaultRule.expectedType);
      setFieldValue(defaultRule.expectedValue);
    }
  }, [defaultRule]);

  useImperativeHandle(
    ref,
    () => ({
      getValue: () => {
        if (operator && fieldType && fieldValue) {
          const resultFieldName: string | null = (fieldName || '').trim();
          if (!resultFieldName && dimension === 'CUSTOMIZE') {
            return null;
          }

          return {
            value: {
              field: resultFieldName || null,
              operator,
              expectedType: fieldType,
              expectedValue: fieldValue,
            },
            key: ruleKey,
          };
        }
        return null;
      },
      key: ruleKey,
    }),
    [dimension, fieldName, fieldType, fieldValue, operator, ruleKey],
  );

  const handleChangeFieldType = useCallback(v => {
    setFieldType(v);
    setFieldValue(undefined);
  }, []);

  return (
    <div className={styles.validateRules}>
      {dimension === 'CUSTOMIZE' && (
        <Input
          className={styles.validateRuleItem}
          placeholder={t('dataDetail.dataQuality.validate.field')}
          value={fieldName}
          onChange={e => {
            setIsUpdatedRule();
            setFieldName(e.target.value);
          }}
        />
      )}

      <Select
        className={c(styles.validateRuleItem, {
          [styles.tableValidateRuleItem]: dimension === 'TABLE' || dimension === 'FIELD',
        })}
        placeholder={t('dataDetail.dataQuality.validate.oprator')}
        value={operator}
        onChange={value => {
          setIsUpdatedRule();
          setOperator(value);
        }}
      >
        {Object.values(ValidateOperatorEnum).map(operatorItem => (
          <Option key={operatorItem} value={operatorItem}>
            {validateOperatorEnumToLocaleString[operatorItem]}
          </Option>
        ))}
      </Select>

      <Select
        className={c(styles.validateRuleItem, {
          [styles.tableValidateRuleItem]: dimension === 'TABLE' || dimension === 'FIELD',
        })}
        value={fieldType}
        onChange={value => {
          setIsUpdatedRule();
          handleChangeFieldType(value);
        }}
      >
        {Object.values(ValidateFieldType).map(type => (
          <Option key={type} value={type}>
            {upperFirst(lowerCase(type))}
          </Option>
        ))}
      </Select>

      {fieldType === ValidateFieldType.BOOLEAN && (
        <Select
          className={c(styles.validateRuleItem, {
            [styles.tableValidateRuleItem]: dimension === 'TABLE' || dimension === 'FIELD',
          })}
          value={fieldValue}
          onChange={value => {
            setIsUpdatedRule();
            setFieldValue(value);
          }}
        >
          <Option value="true">True</Option>
          <Option value="false">False</Option>
        </Select>
      )}

      {fieldType !== ValidateFieldType.BOOLEAN && (
        <Input
          className={c(styles.validateRuleItem, {
            [styles.tableValidateRuleItem]: dimension === 'TABLE' || dimension === 'FIELD',
          })}
          value={fieldValue}
          onChange={e => {
            setIsUpdatedRule();
            setFieldValue(e.target.value);
          }}
        />
      )}
    </div>
  );
};

export default memo(forwardRef(ValidateRule));
