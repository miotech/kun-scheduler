import { GlossaryChild } from '@/rematch/models/glossary';

// model state
export interface ReferenceDataState {
  refTableData: {
    refTableMetaData: ReftableMetaData;
    refData: RefData;
  } | null;
}

// services
export enum ColumnType {
  int = 'int',
  bigint = 'bigint',
  float = 'float',
  double = 'double',
  decimal = 'decimal',
  boolean = 'boolean',
  string = 'string',
  timestamp = 'timestamp',
  date = 'date',
}
export interface Column {
  name: string;
  index: number;
  columnType: ColumnType;
}

export interface ReftableMetaData {
  columns: Column[];
  refTableConstraints: {
    PRIMARY_KEY: string[];
  };
}

export interface Data {
  recordNumber: string;
  values: string[];
}

export interface RefData {
  data: Data[];
}

// components

export interface Cell {
  record: TableRecord;
  dataIndex: number;
  name: string;
  children?: React.ReactNode;
  primaryKey: Boolean;
}

export interface TableColumn {
  name: string;
  index: number;
  columnType: ColumnType;
  dataIndex: number | string;
  primaryKey: boolean;
  width: number;
}

export interface TableRecord {
  recordNumber: string;
  [key: number]: string | null;
}

export interface TableConfigDetail {
  tableId: string;
  databaseName: string;
  tableName: string;
  versionDescription: string;
  ownerList: string[];
  glossaryList: string[];
  versionId: string;
  versionNumber?: number;
  showStatus?: ShowStatus;
  enableEditName: boolean;
}

export enum ShowStatus {
  HISTORY = 'HISTORY',
  PUBLISHED = 'PUBLISHED',
  UNPUBLISHED = 'UNPUBLISHED',
}
export interface LineageDataset {
  [key: string]: string;
}
export interface ReferenceRecords {
  tableName: string;
  versionId: string;
  tableId: string;
  versionNumber: number;
  versionDescription: string;
  updateTime: string;
  updateUser: string;
  showStatus: ShowStatus;
  glossaryList: GlossaryChild[];
  lineageDatasetList: LineageDataset[] | null;
  ownerList: string[];
}

export interface FiledMessage {
  columnName: string;
  lineNumber: string;
  message: string;
  validationType: string;
}
export interface ErrorMessage {
  cellMessageList?: FiledMessage[];
  lineNumber: string;
  rowMessage?: string;
}
export interface ValidationResultVo {
  summary: string;
  validationMessageVoList: ErrorMessage[];
}
export interface DataBase {
  name: string;
}

// Service Definition
export interface FetchRdmDatasParams {
  keyword?: string;
  owners?: string;
  glossaries?: Array<Record<string, string>>;
  startCreateTime?: string;
  endCreateTime?: string;
  startUpdateTime?: string;
  endUpdateTime?: string;
  pageSize: number;
  pageNum: number;
}
