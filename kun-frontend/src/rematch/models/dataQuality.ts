// import { message } from 'antd';
// import { formatMessage } from 'umi';
// import {
//   fetchDatasetDetailService,
//   fetchDatasetColumnsService,
//   pullDatasetService,
//   UpdateDatasetReqBody,
//   updateDatasetService,
//   updateColumnService,
// } from '@/services/datasetDetail';
// import { Watermark } from './dataDiscovery';
// import { Pagination } from './index';
// import { RootDispatch, RootState } from '../store';

export enum ValidateFieldType {
  BOOLEAN = 'BOOLEAN',
  STRING = 'STRING',
  NUMBER = 'NUMBER',
}

export enum ValidateOperatorEnum {
  EQ = '=',
  NE = '!=',
  GTE = '>=',
  GT = '>',
  LTE = '<=',
  LT = '<',
}

export const ValidateOperatorEnumValues: ValidateOperatorEnum[] = Object.values(
  ValidateOperatorEnum,
).map(k => k as ValidateOperatorEnum);

export const validateOperatorEnumToLocaleString: Record<
  ValidateOperatorEnum,
  string
> = {
  [ValidateOperatorEnum.EQ]: '=',
  [ValidateOperatorEnum.NE]: '≠',
  [ValidateOperatorEnum.GTE]: '≥',
  [ValidateOperatorEnum.GT]: '>',
  [ValidateOperatorEnum.LTE]: '≤',
  [ValidateOperatorEnum.LT]: '<',
};

export enum ValidateStatus {
  NO_VALIDATE = -1,
  SUCCESS = 0,
  FAILED = 1,
}

export interface TableDimensionConfigTemplateItem {
  id: string;
  name: string;
}

export interface TableDimensionConfigItem {
  dimension: string;
  templates: TableDimensionConfigTemplateItem[];
}

export interface CustomizeDimensionConfigFieldItem {
  key: string;
  order: number;
  format: string;
  require: boolean;
}

export interface CustomizeDimensionConfigItem {
  dimension: string;
  fields: CustomizeDimensionConfigFieldItem[];
}

export type DimensionConfigItem =
  | TableDimensionConfigItem
  | CustomizeDimensionConfigItem;

export interface ValidateRuleItem {
  fieldName: string | null;
  operator: ValidateOperatorEnum;
  fieldType: ValidateFieldType;
  fieldValue: string;
}

export interface FieldDimensionConfig {
  applyFieldIds: string[];
  templateId: string;
}

export interface TableDimensionConfig {
  templateId: string;
}
export interface CustomizeDimensionConfig {
  sql: string;
}

export type DimensionConfig =
  | CustomizeDimensionConfig
  | TableDimensionConfig
  | FieldDimensionConfig;

export enum DataQualityLevel {
  HIGH = 'HIGH',
  LOW = 'LOW',
  IGNORE = 'IGNORE',
}

export interface RelatedTableItem {
  id: string;
  name: string;
  datasource: string;
  isPrimary?: boolean;
}

export enum DataQualityType {
  Accuracy = 'ACCURACY',
  Completeness = 'COMPLETENESS',
  Consistency = 'CONSISTENCY',
  Timeliness = 'TIMELINESS',
  Uniqueness = 'UNIQUENESS',
}

export const dataQualityTypes: DataQualityType[] = Object.values(
  DataQualityType,
).map(k => k as DataQualityType);

export interface DataQualityBase {
  name: string;
  // level: DataQualityLevel;
  types: DataQualityType[];
  description: string | null;
  dimension: string | null;
  dimensionConfig: DimensionConfig | null;
  validateRules: ValidateRuleItem[];
}

export interface DataQualityReq extends DataQualityBase {
  relatedTableIds: string[];
  primaryDatasetGid?: string;
}

export interface DataQualityResp extends DataQuality {
  relatedTables: RelatedTableItem[];
  id: string;
}

export interface DataQuality extends DataQualityBase {
  relatedTables: RelatedTableItem[];
}
