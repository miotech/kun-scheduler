/* eslint-disable */
// import { Pagination, Sort } from '@/rematch/models';
import SafeUrlAssembler from 'safe-url-assembler';

import {
  DimensionConfigItem,
  DataQualityReq,
  ValidateStatus,
  DataQualityResp,
} from '@/rematch/models/dataQuality';
import { get, post, deleteFunc } from './utils';

export interface FetchDimensionConfigRespBody {
  dimensionConfigs: DimensionConfigItem[];
}

export async function fetchDataQualityService(id: string) {
  const resp = await get<DataQualityResp>(
    SafeUrlAssembler('/data-quality/:id')
      .param({ id })
      .toString(),
  );
  // const resp = {
  //   id,
  //   name: 'XXX',
  //   description: '描述.....',
  //   dimension: 'FIELD',
  //   dimensionConfig: {
  //     templateId: '1',
  //     applyFieldIds: ['1', '2'],
  //   },
  //   validateRules: [
  //     {
  //       fieldName: 'xxx',
  //       operator: '>=',
  //       fieldType: 'NUMBER',
  //       fieldValue: 'true',
  //     },
  //   ],
  //   relatedTables: [{ name: 'aaaa', id: '1' }],
  // };
  return resp;
}

export async function fetchDimensionConfig(datasourceType: string) {
  const resp = await get<FetchDimensionConfigRespBody>(
    '/data-quality/dimension/get-config',
    { datasourceType },
  );
  // const resp = {
  //   dimensionConfigs: [
  //     {
  //       dimension: 'TABLE',
  //       templates: [{ id: '1', name: 'xxx' }],
  //     },
  //     {
  //       dimension: 'FIELD',
  //       templates: [{ id: '2', name: 'sss' }],
  //     },
  //     {
  //       dimension: 'CUSTOMIZE',
  //       fields: [{ key: 'sql', order: 1, format: 'SQL', require: true }],
  //     },
  //   ],
  // };
  return resp;
}

export interface FetchValidateSQLServiceRespBody {
  validateStatus: ValidateStatus;
}

export async function fetchValidateSQLService(
  sqlText: string,
  datasetId: string,
) {
  const resp = await post<FetchValidateSQLServiceRespBody>('/sql/validate', {
    sqlText,
    datasetId: datasetId,
  });
  // const resp = {
  //   validateStatus: true,
  // };
  return resp;
}

export async function addDataQualityService(params: DataQualityReq) {
  const resp = await post<{ id: string }>('/data-quality/add', params);
  // const resp = {
  //   id: 'dafdasfasdf',
  // };
  return resp;
}

export interface editQualityReq extends DataQualityReq {}

export async function editQualityService(id: string, params: editQualityReq) {
  const resp = await post<{ id: string }>(
    SafeUrlAssembler('/data-quality/:id/edit')
      .param({ id })
      .toString(),
    params,
  );
  // const resp = {
  //   id: 'dafdasfasdf',
  // };
  return resp;
}

export interface DeleteQualityReq {
  datasetId: string;
}

export async function deleteQualityService(
  id: string,
  params: DeleteQualityReq,
) {
  const resp = await deleteFunc<{ id: string }>(
    SafeUrlAssembler('/data-quality/:id/delete')
      .param({ id })
      .toString(),
    params,
  );
  // const resp = {
  //   id: 'dafdasfasdf',
  // };
  return resp;
}
