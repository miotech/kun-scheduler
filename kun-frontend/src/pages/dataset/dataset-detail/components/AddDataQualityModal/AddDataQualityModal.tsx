import React, { memo, useEffect, useState, useCallback, useMemo, useRef } from 'react';
import { Controlled as CodeMirror } from 'react-codemirror2';
import { ExclamationCircleOutlined } from '@ant-design/icons';

import { Modal, Spin, Input, Radio, Select, message, Button, Tooltip, Checkbox } from 'antd';
import { RadioChangeEvent } from 'antd/lib/radio';
import uniqueId from 'lodash/uniqueId';
import {
  fetchDimensionConfig,
  fetchValidateSQLService,
  addDataQualityService,
  fetchDataQualityService,
  editQualityService,
  fetchDataQualityHistoriesService,
} from '@/services/dataQuality';
import {
  DimensionConfigItem,
  DataQuality,
  RelatedTableItem,
  TableDimensionConfigItem,
  CustomizeDimensionConfigItem,
  CustomizeDimensionConfigFieldItem,
  ValidateRuleItem,
  DataQualityReq,
  DimensionConfig,
  ValidateStatus,
  TableDimensionConfig,
  FieldDimensionConfig,
  CustomizeDimensionConfig,
  DataQualityType,
  dataQualityTypes,
  DataQualityHistory,
} from '@/rematch/models/dataQuality';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';

import useI18n from '@/hooks/useI18n';
import { Column } from '@/rematch/models/datasetDetail';
import { fetchDatasetColumnsService } from '@/services/datasetDetail';

import ValidateRule, { ValidateRuleHandle } from '../ValidateRule/ValidateRule';
import RelatedTablesComp from '../RelatedTablesComp/RelatedTablesComp';

import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/material.css';
import 'codemirror/mode/sql/sql';

import styles from './AddDataQualityModal.less';
import HistoryTable from '../HistoryTable/HistoryTable';

const { TextArea } = Input;
const { Option } = Select;

interface Props {
  visible: boolean;
  onClose: () => void;
  onConfirm: () => void;
  datasourceType: string | null;
  relatedTable: RelatedTableItem;
  datasetId: string;
  dataQualityId: string | null;
}

export default memo(function AddDataQualityModal({
  visible,
  onClose,
  onConfirm,
  datasourceType,
  relatedTable,
  datasetId,
  dataQualityId, // 如果有这个id 那么要在打开弹窗的时候进行拉取数据, 并且这个算编辑弹框
}: Props) {
  const t = useI18n();

  const [data, setData] = useState<DataQuality>(() => ({
    name: '',
    types: [],
    // level: DataQualityLevel.LOW,
    description: '',
    isBlocking: false,
    dimension: null,
    dimensionConfig: null, // 这里暂时用不到, 如果后面要做edit功能, 这里用useeffect赋值, 然后再给selectedTemplateId和customizeInputtingObj赋值
    validateRules: [],
    relatedTables: [relatedTable],
  }));

  const validateRuleRefs = useRef<ValidateRuleHandle[]>([]);

  // 记录是否改变了sql
  const [isUpdatedSQL, setIsUpdatedSQL] = useState(false);
  const [isUpdatedRule, setIsUpdatedRule] = useState(false);

  useEffect(() => {
    setIsUpdatedSQL(false);
    setIsUpdatedRule(false);
  }, [visible]);

  // 原始 dataquality 信息
  const [oldData, setOldData] = useState<DataQuality | null>(null);
  const [oldDataLoading, setOldDataLoading] = useState(false);

  // dataset columns
  const [allColumns, setAllColumns] = useState<Column[]>([]);
  const [fetchColumnsLoading, setFetchColumnsLoading] = useState(false);

  const [config, setConfig] = useState<DimensionConfigItem[]>([]);
  const [configLoading, setConfigLoading] = useState(false);

  const [dimensionList, setDimensionList] = useState<string[]>([]);

  const [selectedTableTemplateId, setSelectedTableTemplateId] = useState<string | undefined>(undefined);
  const [selectedFieldTemplateId, setSelectedFieldTemplateId] = useState<string | undefined>(undefined);
  const [selectedApplyFieldIds, setSelectedApplyFieldIds] = useState<string[]>([]);

  const [validateSQLStatus, setValidateSQLStatus] = useState<ValidateStatus>(ValidateStatus.NO_VALIDATE);

  const [validateRuleError, setValidateRuleError] = useState('');

  const [validateSQLLoading, setValidateSQLLoading] = useState(false);
  const [validateSQLErrorMsg, setValidateSQLErrorMsg] = useState('');

  const [primaryDatasetGid, setPrimaryDatasetGid] = useState<string | null>(null);

  const [customizeInputtingObj, setCustomizeInputtingObj] = useState<any>({});

  const [history, setHistory] = useState<DataQualityHistory[]>([]);
  const [fetchHistoryLoading, setFetchHistoryLoading] = useState<boolean>(false);

  const [validateRuleList, setValidateRuleList] = useState<{ key: string; rule?: ValidateRuleItem }[]>([
    { key: uniqueId('validate-rule') },
  ]);

  const [confirmLoading, setConfirmLoading] = useState(false);

  const fetchDataQuality = async (id: string) => {
    const resp = await fetchDataQualityService(id);
    return resp;
  };

  // 初始化数据 如果是编辑, 还要拿到原始数据并初始化
  useEffect(() => {
    const func = async () => {
      if (!visible) {
        setData({
          name: '',
          // level: DataQualityLevel.LOW,
          types: [],
          description: '',
          isBlocking: false,
          dimension: null,
          dimensionConfig: null,
          validateRules: [],
          relatedTables: [relatedTable],
        });
        setOldData(null);
      } else if (dataQualityId) {
        setOldDataLoading(true);
        const legacyData = await fetchDataQuality(dataQualityId);
        setOldDataLoading(false);
        if (legacyData) {
          setOldData(legacyData);
          if (legacyData.relatedTables) {
            const primaryTable = legacyData.relatedTables.find(i => i.isPrimary);
            if (primaryTable) {
              setPrimaryDatasetGid(primaryTable.id);
            } else {
              setPrimaryDatasetGid(datasetId);
            }
          }
          setData({
            name: legacyData.name,
            // level: DataQualityLevel.LOW,
            types: legacyData.types,
            isBlocking: legacyData.isBlocking,
            description: legacyData.description,
            dimension: legacyData.dimension,
            dimensionConfig: legacyData.dimensionConfig,
            validateRules: legacyData.validateRules as ValidateRuleItem[],
            relatedTables: legacyData.relatedTables,
          });
        }
      } else {
        setData((d: DataQuality) => ({
          ...d,
          relatedTables: [relatedTable],
        }));
      }
    };

    func();
  }, [visible, relatedTable, dataQualityId, datasetId]);

  // 如果是编辑状态, 拉取历史
  useEffect(() => {
    const fetchFunc = async () => {
      if (dataQualityId) {
        try {
          setFetchHistoryLoading(true);
          const resp = await fetchDataQualityHistoriesService([dataQualityId]);
          setHistory(resp?.[0]?.historyList);
        } catch (e) {
          // do nothing
        } finally {
          setFetchHistoryLoading(false);
        }
      }
    };
    fetchFunc();
    return () => {
      setHistory([]);
    };
  }, [dataQualityId, visible]);

  // 拿到所有的columns 在 dimension Field 模式下用于applyFields
  useEffect(() => {
    const fetchAllColumns = async () => {
      setFetchColumnsLoading(true);
      const resp = await fetchDatasetColumnsService(datasetId, '', {
        pageSize: 100000,
        pageNumber: 1,
      });
      setFetchColumnsLoading(false);
      if (resp) {
        setAllColumns(resp.columns || []);
      }
    };
    if (!allColumns.length) {
      fetchAllColumns();
    }
  }, [allColumns.length, data.dimension, datasetId]);

  // 拿到不同 dimension下的模板数据 或者字段信息
  useEffect(() => {
    const fetchConfigFunc = async () => {
      setConfigLoading(true);
      const resp = await fetchDimensionConfig(datasourceType!);
      setConfigLoading(false);
      if (resp) {
        setConfig(resp.dimensionConfigs);
        const dimensions = resp.dimensionConfigs.map(i => i.dimension);
        setDimensionList(dimensions);
      }
    };
    if ((!config || config.length === 0) && visible && datasourceType) {
      fetchConfigFunc();
    }
  }, [config, datasourceType, visible]);

  // 如果关闭弹框  那么初始化弹框内容
  useEffect(() => {
    setValidateSQLStatus(ValidateStatus.NO_VALIDATE);
    setValidateRuleError('');
    setSelectedTableTemplateId(undefined);
    setSelectedFieldTemplateId(undefined);
    setSelectedApplyFieldIds([]);
    setCustomizeInputtingObj({});
  }, [visible]);

  useEffect(() => {
    setValidateSQLStatus(ValidateStatus.NO_VALIDATE);
  }, [customizeInputtingObj.sql, data.dimension]);

  // 切换 dimension
  useEffect(() => {
    if (data.dimensionConfig && (!oldData || (oldData && oldData.dimension === data.dimension))) {
      if (data.dimension === 'TABLE') {
        setSelectedTableTemplateId((data.dimensionConfig as TableDimensionConfig).templateId);
      }
      if (data.dimension === 'FIELD') {
        setSelectedFieldTemplateId((data.dimensionConfig as FieldDimensionConfig).templateId);
        setSelectedApplyFieldIds((data.dimensionConfig as FieldDimensionConfig).applyFieldIds);
      }

      if (data.dimension === 'CUSTOMIZE') {
        setCustomizeInputtingObj({
          sql: (data.dimensionConfig as CustomizeDimensionConfig).sql,
        });
      }
    }
  }, [data.dimension, data.dimensionConfig, oldData]);

  // 切换 dimension 时填充 validateRules 的逻辑
  useEffect(() => {
    if (dataQualityId) {
      if (oldData && oldData.dimension === data.dimension) {
        const rules = oldData.validateRules.map(rule => ({
          key: uniqueId('validate-rule'),
          rule,
        }));
        if (rules.length > 0) {
          setValidateRuleList(rules);
          return;
        }
      }
    }
    setValidateRuleList([{ key: uniqueId('validate-rule') }]);
  }, [data.dimension, dataQualityId, oldData]);

  // 初始化 validateRuleList (代码用, 加入一个key)
  useEffect(() => {
    const newValidateRuleList: {
      key: string;
      rule?: ValidateRuleItem | undefined;
    }[] = data.validateRules.map((rule: ValidateRuleItem) => ({
      key: uniqueId('validate-rule'),
      rule,
    }));
    if (newValidateRuleList.length > 0) {
      setValidateRuleList(newValidateRuleList);
    }
  }, [data.validateRules]);

  const onChangeData = (v: any, k: keyof DataQuality) => {
    setData((data1: DataQuality) => ({
      ...data1,
      [k]: v,
    }));
  };

  const selectedDimension = useMemo(() => data.dimension, [data.dimension]);
  const currentConfig = useMemo(() => config.find(i => i.dimension === selectedDimension), [config, selectedDimension]);

  const onChangeFunctionsMap = useMemo(
    () => ({
      name: (e: React.ChangeEvent<HTMLInputElement>) => onChangeData(e.target.value, 'name'),
      types: (v: DataQualityType[]) => onChangeData(v, 'types'),
      description: (e: React.ChangeEvent<HTMLTextAreaElement>) => onChangeData(e.target.value, 'description'),
      dimension: (e: RadioChangeEvent) => {
        onChangeData(e.target.value, 'dimension');
        if (e.target.value === 'CUSTOMIZE') {
          setData((d: DataQuality) => ({
            ...d,
            relatedTables: oldData?.relatedTables || [relatedTable],
          }));
        } else {
          setData((d: DataQuality) => ({
            ...d,
            relatedTables: [relatedTable],
          }));
        }
      },
      isBlocking: (e: CheckboxChangeEvent) => onChangeData(e.target.checked, 'isBlocking'),
    }),
    [oldData?.relatedTables, relatedTable],
  );

  const handleValidateSQL = useCallback(
    async (sql: string) => {
      const shouldRuleKeys = validateRuleList.map(i => i.key);
      const allRuleList = validateRuleRefs.current
        .map(ruleRef => ruleRef.getValue())
        .filter(i => !!i)
        .filter(i => shouldRuleKeys.includes(i!.key))
        .map(i => i!.value);
      setValidateSQLLoading(true);
      const validateSQLLoadingFunc = message.loading(t('common.loading'), 0);
      try {
        const resp = await fetchValidateSQLService(sql, datasetId, allRuleList);
        setValidateSQLLoading(false);
        validateSQLLoadingFunc();
        if (resp) {
          setValidateSQLStatus(resp.validateStatus);
          if (resp.validateStatus === ValidateStatus.FAILED) {
            setValidateSQLErrorMsg(resp.validateMessage);
          } else {
            setData((d: DataQuality) => ({
              ...d,
              relatedTables: resp.relatedTables,
            }));
            const primaryTable = resp.relatedTables.find(table => table.isPrimary);
            if (primaryTable) {
              setPrimaryDatasetGid(primaryTable.id);
            }
          }
        }
      } catch (e) {
        // do nothing
      } finally {
        setValidateSQLLoading(false);
      }
    },
    [datasetId, validateRuleList, validateRuleRefs, t],
  );

  const getCustomizeCompFunc = useCallback(
    (fieldObj: CustomizeDimensionConfigFieldItem) => {
      const { key, format } = fieldObj;
      if (format === 'SQL') {
        return (
          <div className={styles.codeEditorContainer}>
            <CodeMirror
              className={styles.codeEditor}
              value={customizeInputtingObj[key]}
              options={{
                mode: 'sql',
                theme: 'material',
                lineNumbers: true,
                lineWrapping: true,
              }}
              onBeforeChange={(_editor, _data, value) => {
                setIsUpdatedSQL(true);
                setCustomizeInputtingObj((obj: any) => ({
                  ...obj,
                  [key]: value,
                }));
              }}
            />
          </div>
        );
      }
      return null;
    },
    [customizeInputtingObj],
  );

  const dimensionView = useMemo(() => {
    if (!currentConfig) {
      return null;
    }
    if (data.dimension === 'TABLE' || data.dimension === 'FIELD') {
      const { templates } = currentConfig as TableDimensionConfigItem;
      const selectedTempId = data.dimension === 'TABLE' ? selectedTableTemplateId : selectedFieldTemplateId;

      const setTemplateIdFunc = data.dimension === 'TABLE' ? setSelectedTableTemplateId : setSelectedFieldTemplateId;
      return (
        <>
          {data.dimension === 'FIELD' && (
            <Spin spinning={fetchColumnsLoading}>
              <div className={styles.fieldItem}>
                <div className={styles.fieldTitle}>
                  {t('dataDetail.dataQuality.applyFields')}
                  <span className={styles.required}>*</span>
                </div>
                <div className={styles.dimensionFieldContent}>
                  <Select
                    style={{ width: '100%' }}
                    mode="multiple"
                    value={selectedApplyFieldIds}
                    onChange={setSelectedApplyFieldIds}
                  >
                    {allColumns.map(column => (
                      <Option key={column.id} value={column.id}>
                        {column.name}
                      </Option>
                    ))}
                  </Select>
                </div>
              </div>
            </Spin>
          )}
          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.template')}
              <span className={styles.required}>*</span>
            </div>
            <div className={styles.dimensionFieldContent}>
              <Select
                key={data.dimension}
                style={{ width: '100%' }}
                value={selectedTempId}
                onChange={setTemplateIdFunc}
              >
                {templates.map(temp => (
                  <Option key={temp.id} value={temp.id}>
                    {temp.name}
                  </Option>
                ))}
              </Select>
            </div>
          </div>
        </>
      );
    }

    if (data.dimension === 'CUSTOMIZE') {
      const { fields } = currentConfig as CustomizeDimensionConfigItem;
      return fields.map(field => (
        <div key={field.key} className={styles.fieldItem}>
          <div className={styles.fieldTitle}>
            {t(`dataDetail.dataQuality.dimension.${field.key}`)}
            {field.require && <span className={styles.required}>*</span>}
          </div>
          <div className={styles.dimensionFieldContent}>{getCustomizeCompFunc(field)}</div>
        </div>
      ));
    }

    return null;
  }, [
    allColumns,
    currentConfig,
    data.dimension,
    fetchColumnsLoading,
    getCustomizeCompFunc,
    selectedApplyFieldIds,
    selectedFieldTemplateId,
    selectedTableTemplateId,
    t,
  ]);

  const pushTovalidateRuleRefList = useCallback((ref: ValidateRuleHandle) => {
    if (ref) {
      const index = validateRuleRefs.current.findIndex(i => i.key === ref.key);
      if (index === -1) {
        validateRuleRefs.current.push(ref);
      } else {
        validateRuleRefs.current[index] = ref;
      }
    }
  }, []);

  const handleClickDeleteRule = useCallback((key: string) => {
    setValidateRuleList(list => list.filter(i => i.key !== key));
  }, []);

  const handleClickAddRule = useCallback(() => {
    setValidateRuleList(list => [...list, { key: uniqueId('validate-rule') }]);
  }, []);

  const handleCancel = useCallback(() => {
    onClose();
  }, [onClose]);

  const handleConfirm = useCallback(async () => {
    const shouldRuleKeys = validateRuleList.map(i => i.key);
    const allRuleList = validateRuleRefs.current
      .map(ruleRef => ruleRef.getValue())
      .filter(i => !!i)
      .filter(i => shouldRuleKeys.includes(i!.key))
      .map(i => i!.value);

    if (allRuleList.length === 0) {
      setValidateRuleError(t('dataDetail.dataQuality.validate.noUseable'));
      return;
    }

    const {
      name,
      types,
      // level
      description,
      dimension,
      // dimensionConfig,
      // validateRules,
      relatedTables,
      isBlocking,
    } = data;

    let currentDimensionConfig: DimensionConfig;

    if (dimension === 'TABLE') {
      currentDimensionConfig = {
        templateId: selectedTableTemplateId || '',
      };
    }
    if (dimension === 'FIELD') {
      currentDimensionConfig = {
        templateId: selectedFieldTemplateId || '',
        applyFieldIds: selectedApplyFieldIds || [],
      };
    }

    if (dimension === 'CUSTOMIZE') {
      currentDimensionConfig = { sql: customizeInputtingObj.sql };
    }

    const params: DataQualityReq = {
      name,
      types,
      description,
      dimension,
      isBlocking,
      dimensionConfig: currentDimensionConfig!,
      validateRules: allRuleList,
      relatedTableIds: relatedTables.map((i: RelatedTableItem) => i.id),
      primaryDatasetGid: dimension === 'CUSTOMIZE' && primaryDatasetGid ? primaryDatasetGid : datasetId,
    };

    setConfirmLoading(true);
    const loadingFunc = message.loading(t('common.loading'), 0);
    try {
      if (!dataQualityId) {
        const resp = await addDataQualityService(params);
        if (resp) {
          onClose();
          onConfirm();
          message.success(t('common.operateSuccess'));
        }
      } else {
        const resp = await editQualityService(dataQualityId, params);
        if (resp) {
          onClose();
          onConfirm();
          message.success(t('common.operateSuccess'));
        }
      }
    } catch (e) {
      // do nothing
    } finally {
      loadingFunc();
      setConfirmLoading(false);
    }
  }, [
    customizeInputtingObj.sql,
    data,
    dataQualityId,
    datasetId,
    onClose,
    onConfirm,
    primaryDatasetGid,
    selectedApplyFieldIds,
    selectedFieldTemplateId,
    selectedTableTemplateId,
    t,
    validateRuleList,
  ]);

  const disabledConfirmButton = useMemo(() => {
    let disabled = false;
    const { name, dimension } = data;
    if (!name || !dimension) {
      disabled = true;
    }
    if (dimension === 'CUSTOMIZE' && !customizeInputtingObj.sql) {
      disabled = true;
    }
    // 未校验或者校验失败
    const noOrErrorValidate =
      validateSQLStatus === ValidateStatus.FAILED || validateSQLStatus === ValidateStatus.NO_VALIDATE;
    // 新建的时候
    if (!dataQualityId && dimension === 'CUSTOMIZE' && noOrErrorValidate) {
      disabled = true;
    }
    // 更新的时候
    if (dataQualityId && dimension === 'CUSTOMIZE' && (isUpdatedSQL || isUpdatedRule) && noOrErrorValidate) {
      disabled = true;
    }

    if (
      dimension === 'FIELD' &&
      (!selectedFieldTemplateId || !selectedApplyFieldIds || selectedApplyFieldIds.length === 0)
    ) {
      disabled = true;
    }
    if (dimension === 'TABLE' && !selectedTableTemplateId) {
      disabled = true;
    }

    return disabled;
  }, [
    customizeInputtingObj.sql,
    data,
    dataQualityId,
    isUpdatedSQL,
    isUpdatedRule,
    selectedApplyFieldIds,
    selectedFieldTemplateId,
    selectedTableTemplateId,
    validateSQLStatus,
  ]);

  return (
    <Modal
      visible={visible}
      onCancel={onClose}
      closable={false}
      footer={null}
      title={null}
      bodyStyle={{ padding: 0 }}
      width={685}
    >
      <Spin spinning={configLoading || oldDataLoading}>
        <div className={styles.titleRow}>
          {dataQualityId ? t('dataDetail.dataQuality.editQuality') : t('dataDetail.dataQuality.newQuality')}
        </div>

        <div className={styles.fieldArea}>
          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.name')}
              <span className={styles.required}>*</span>
            </div>
            <div className={styles.fieldContent}>
              <Input value={data.name} onChange={onChangeFunctionsMap.name} />
            </div>
          </div>

          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>{t('dataDetail.dataQuality.type')}</div>
            <div className={styles.fieldContent}>
              <Select
                mode="multiple"
                style={{ width: '100%' }}
                value={data?.types?.length > 0 ? data.types : undefined}
                onChange={onChangeFunctionsMap.types}
              >
                {dataQualityTypes.map(type => (
                  <Option key={type} value={type}>
                    {t(`dataDetail.dataQuality.type.${type}`)}
                  </Option>
                ))}
              </Select>
            </div>
          </div>

          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>{t('dataDetail.dataQuality.description')}</div>
            <div className={styles.fieldContent}>
              <TextArea value={data.description || ''} onChange={onChangeFunctionsMap.description} />
            </div>
          </div>

          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.dimension')}
              <span className={styles.required}>*</span>
            </div>
            <div className={styles.fieldContent}>
              <Radio.Group onChange={onChangeFunctionsMap.dimension} value={data.dimension}>
                {dimensionList.map(i => {
                  const dimensionConfig = config.find(item => item.dimension === i);
                  let disabled = false;
                  if (
                    (i === 'TABLE' || i === 'FIELD') &&
                    (!(dimensionConfig as TableDimensionConfigItem)?.templates ||
                      (dimensionConfig as TableDimensionConfigItem).templates?.length === 0)
                  ) {
                    disabled = true;
                  }
                  if (i === 'FIELD' && (!allColumns || allColumns.length === 0)) {
                    disabled = true;
                  }
                  return (
                    <Radio className={styles.centerItem} disabled={disabled} key={i} value={i}>
                      {t(`dataDetail.dataQuality.dimension.${i}`)}
                    </Radio>
                  );
                })}
              </Radio.Group>
            </div>
          </div>

          <div className={styles.separator} />

          {dimensionView}

          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.validateRule')}
              <span className={styles.required}>*</span>
            </div>
            <div className={styles.validateFieldContentContainer}>
              {validateRuleList.map((validateRules, index) => (
                <div key={validateRules.key} className={styles.validateFieldContent}>
                  <div className={styles.validateRuleIndex}>{index + 1}</div>
                  <div className={styles.validateRuleContainer}>
                    <ValidateRule
                      ref={pushTovalidateRuleRefList}
                      ruleKey={validateRules.key}
                      dimension={data.dimension}
                      setIsUpdatedRule={() => {
                        setIsUpdatedRule(true);
                        setValidateSQLStatus(ValidateStatus.NO_VALIDATE);
                      }}
                      defaultRule={validateRules.rule}
                    />
                  </div>

                  <Button style={{ width: 32, padding: 0 }} onClick={() => handleClickDeleteRule(validateRules.key)}>
                    -
                  </Button>
                </div>
              ))}
              <Button onClick={handleClickAddRule}>{`+ ${t('dataDetail.dataQuality.validate.add')}`}</Button>
              <div className={styles.sqlValidateButtonRow}>
                {validateSQLStatus === ValidateStatus.SUCCESS && (
                  <span className={styles.validateSQLSuccess}>
                    {t('dataDetail.dataQuality.dimension.validate.success')}
                  </span>
                )}
                {validateSQLStatus === ValidateStatus.FAILED && (
                  <span className={styles.validateSQLFaild}>
                    {t('dataDetail.dataQuality.dimension.validate.failed')}
                    <Tooltip title={validateSQLErrorMsg}>
                      <ExclamationCircleOutlined style={{ marginLeft: 4 }} />
                    </Tooltip>
                  </span>
                )}
                <Button disabled={validateSQLLoading} onClick={() => handleValidateSQL(customizeInputtingObj.sql)}>
                  {t('dataDetail.dataQuality.dimension.validate')}
                </Button>
              </div>
              {validateRuleError && <span className={styles.validateRuleError}>{validateRuleError}</span>}
            </div>
          </div>

          <div className={styles.separator} />
          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>{t('dataDetail.dataQuality.caseBlock')}</div>
            <div className={styles.fieldContent}>
              <Checkbox checked={data.isBlocking} onChange={onChangeFunctionsMap.isBlocking}>
                {t('dataDetail.dataQuality.caseBlock.checkboxTitle')}
              </Checkbox>
            </div>
          </div>

          <div className={styles.separator} />

          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.relatedTable')}
              <span className={styles.required}>*</span>
            </div>
            <div className={styles.fieldContent}>
              <RelatedTablesComp
                selectedTables={data.relatedTables || []}
                defaultTableId={data.dimension === 'CUSTOMIZE' ? primaryDatasetGid : relatedTable.id}
              />
            </div>
          </div>

          {dataQualityId && (
            <>
              <div className={styles.separator} />

              <div className={styles.fieldItem}>
                <div className={styles.fieldTitle}>{t('dataDetail.dataQuality.history')}</div>
                <div className={styles.fieldContent}>
                  <HistoryTable data={history} loading={fetchHistoryLoading} />
                </div>
              </div>
            </>
          )}
        </div>
        <div className={styles.buttonRow}>
          <Button size="large" style={{ marginRight: 16 }} onClick={handleCancel}>
            {t('common.button.cancel')}
          </Button>
          <Button
            disabled={disabledConfirmButton || confirmLoading}
            size="large"
            type="primary"
            onClick={handleConfirm}
          >
            {t('common.button.confirm')}
          </Button>
        </div>
      </Spin>
    </Modal>
  );
});
