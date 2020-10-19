import React, {
  memo,
  useEffect,
  useState,
  useCallback,
  useMemo,
  useRef,
} from 'react';
import { Controlled as CodeMirror } from 'react-codemirror2';

import { Modal, Spin, Input, Radio, Select, message, Button } from 'antd';
import { RadioChangeEvent } from 'antd/lib/radio';
import uniqueId from 'lodash/uniqueId';
import {
  fetchDimensionConfig,
  fetchValidateSQLService,
  addDataQualityService,
  fetchDataQualityService,
  editQualityService,
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
} from '@/rematch/models/dataQuality';

import useI18n from '@/hooks/useI18n';
import { Column } from '@/rematch/models/datasetDetail';
import { fetchDatasetColumnsService } from '@/services/datasetDetail';

import ValidateRule, { ValidateRuleHandle } from '../ValidateRule/ValidateRule';
import RelatedTablesComp from '../RelatedTablesComp/RelatedTablesComp';

import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/material.css';
import 'codemirror/mode/sql/sql';

import styles from './AddDataQualityModal.less';

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
    dimension: null,
    dimensionConfig: null, // 这里暂时用不到, 如果后面要做edit功能, 这里用useeffect赋值, 然后再给selectedTemplateId和customizeInputtingObj赋值
    validateRules: [],
    relatedTables: [relatedTable],
  }));

  // 原始 dataquality 信息
  const [oldData, setOldData] = useState<DataQuality | null>(null);
  const [oldDataLoading, setOldDataLoading] = useState(false);

  // dataset columns
  const [allColumns, setAllColumns] = useState<Column[]>([]);
  const [fetchColumnsLoading, setFetchColumnsLoading] = useState(false);

  const [config, setConfig] = useState<DimensionConfigItem[]>([]);
  const [configLoading, setConfigLoading] = useState(false);

  const [dimensionList, setDimensionList] = useState<string[]>([]);

  const [selectedTableTemplateId, setSelectedTableTemplateId] = useState<
    string | undefined
  >(undefined);
  const [selectedFieldTemplateId, setSelectedFieldTemplateId] = useState<
    string | undefined
  >(undefined);
  const [selectedApplyFieldIds, setSelectedApplyFieldIds] = useState<string[]>(
    [],
  );

  const [validateSQLStatus, setValidateSQLStatus] = useState<ValidateStatus>(
    ValidateStatus.NO_VALIDATE,
  );

  const [validateRuleError, setValidateRuleError] = useState('');

  const [validateSQLLoading, setValidateSQLLoading] = useState(false);

  const [customizeInputtingObj, setCustomizeInputtingObj] = useState<any>({});

  const [validateRuleList, setValidateRuleList] = useState<
    { key: string; rule?: ValidateRuleItem }[]
  >([{ key: uniqueId('validate-rule') }]);

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
          dimension: null,
          dimensionConfig: null,
          validateRules: [],
          relatedTables: [relatedTable],
        });
        setOldData(null);
      } else if (dataQualityId) {
        setOldDataLoading(true);
        const oldData1 = await fetchDataQuality(dataQualityId);
        setOldDataLoading(false);
        if (oldData1) {
          setOldData(oldData1);
          setData({
            name: oldData1.name,
            // level: DataQualityLevel.LOW,
            types: oldData1.types,
            description: oldData1.description,
            dimension: oldData1.dimension,
            dimensionConfig: oldData1.dimensionConfig,
            validateRules: oldData1.validateRules as ValidateRuleItem[],
            relatedTables: oldData1.relatedTables,
          });
        }
      } else {
        setData(d => ({
          ...d,
          relatedTables: [relatedTable],
        }));
      }
    };

    func();
  }, [visible, relatedTable, dataQualityId]);

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
    if (!allColumns.length && data.dimension === 'FIELD') {
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

  // 切换 dimension
  useEffect(() => {
    if (
      data.dimensionConfig &&
      (!oldData || (oldData && oldData.dimension === data.dimension))
    ) {
      if (data.dimension === 'TABLE') {
        setSelectedTableTemplateId(
          (data.dimensionConfig as TableDimensionConfig).templateId,
        );
      }
      if (data.dimension === 'FIELD') {
        setSelectedFieldTemplateId(
          (data.dimensionConfig as FieldDimensionConfig).templateId,
        );
        setSelectedApplyFieldIds(
          (data.dimensionConfig as FieldDimensionConfig).applyFieldIds,
        );
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
    }[] = data.validateRules.map(rule => ({
      key: uniqueId('validate-rule'),
      rule,
    }));
    if (newValidateRuleList.length > 0) {
      setValidateRuleList(newValidateRuleList);
    }
  }, [data.validateRules]);

  const onChangeData = (v: any, k: keyof DataQuality) => {
    setData(data1 => ({
      ...data1,
      [k]: v,
    }));
  };

  const selectedDimension = useMemo(() => data.dimension, [data.dimension]);
  const currentConfig = useMemo(
    () => config.find(i => i.dimension === selectedDimension),
    [config, selectedDimension],
  );

  const onChangeFunctionsMap = useMemo(
    () => ({
      name: (e: React.ChangeEvent<HTMLInputElement>) =>
        onChangeData(e.target.value, 'name'),
      types: (v: DataQualityType[]) => onChangeData(v, 'types'),
      description: (e: React.ChangeEvent<HTMLTextAreaElement>) =>
        onChangeData(e.target.value, 'description'),
      dimension: (e: RadioChangeEvent) => {
        onChangeData(e.target.value, 'dimension');
        if (e.target.value === 'CUSTOMIZE') {
          setData(d => ({
            ...d,
            relatedTables: oldData?.relatedTables || [relatedTable],
          }));
        } else {
          setData(d => ({
            ...d,
            relatedTables: [relatedTable],
          }));
        }
      },
    }),
    [oldData?.relatedTables, relatedTable],
  );

  const handleValidateSQL = useCallback(
    async (sql: string) => {
      setValidateSQLLoading(true);
      const validateSQLLoadingFunc = message.loading(t('common.loading'), 0);
      const resp = await fetchValidateSQLService(sql, datasetId);
      setValidateSQLLoading(false);
      validateSQLLoadingFunc();
      if (resp) {
        setValidateSQLStatus(resp.validateStatus);
      }
    },
    [datasetId, t],
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
                setCustomizeInputtingObj((obj: any) => ({
                  ...obj,
                  [key]: value,
                }));
              }}
            />
            <div className={styles.sqlValidateButtonRow}>
              {validateSQLStatus === ValidateStatus.SUCCESS && (
                <span className={styles.validateSQLSuccess}>
                  {t('dataDetail.dataQuality.dimension.validate.success')}
                </span>
              )}
              {validateSQLStatus === ValidateStatus.FAILD && (
                <span className={styles.validateSQLFaild}>
                  {t('dataDetail.dataQuality.dimension.validate.faild')}
                </span>
              )}
              <Button
                disabled={validateSQLLoading}
                onClick={() => handleValidateSQL(customizeInputtingObj[key])}
              >
                {t('dataDetail.dataQuality.dimension.validate')}
              </Button>
            </div>
          </div>
        );
      }
      return null;
    },
    [
      customizeInputtingObj,
      handleValidateSQL,
      t,
      validateSQLLoading,
      validateSQLStatus,
    ],
  );

  const dimensionView = useMemo(() => {
    if (!currentConfig) {
      return null;
    }
    if (data.dimension === 'TABLE' || data.dimension === 'FIELD') {
      const { templates } = currentConfig as TableDimensionConfigItem;
      const selectedTempId =
        data.dimension === 'TABLE'
          ? selectedTableTemplateId
          : selectedFieldTemplateId;

      const setTemplateIdFunc =
        data.dimension === 'TABLE'
          ? setSelectedTableTemplateId
          : setSelectedFieldTemplateId;
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
          <div className={styles.dimensionFieldContent}>
            {getCustomizeCompFunc(field)}
          </div>
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

  const validateRuleRefs = useRef<ValidateRuleHandle[]>([]);

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

  const handleChangeRelatedTables = useCallback((v: RelatedTableItem[]) => {
    setData(i => ({
      ...i,
      relatedTables: v,
    }));
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
      dimensionConfig: currentDimensionConfig!,
      validateRules: allRuleList,
      relatedTableIds: relatedTables.map(i => i.id),
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
    onClose,
    onConfirm,
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
    if (
      dimension === 'CUSTOMIZE' &&
      (validateSQLStatus === ValidateStatus.FAILD ||
        validateSQLStatus === ValidateStatus.NO_VALIDATE)
    ) {
      disabled = true;
    }

    if (
      dimension === 'FIELD' &&
      (!selectedFieldTemplateId ||
        !selectedApplyFieldIds ||
        selectedApplyFieldIds.length === 0)
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
          {dataQualityId
            ? t('dataDetail.dataQuality.editQuality')
            : t('dataDetail.dataQuality.newQuality')}
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
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.type')}
            </div>
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
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.description')}
            </div>
            <div className={styles.fieldContent}>
              <TextArea
                value={data.description || ''}
                onChange={onChangeFunctionsMap.description}
              />
            </div>
          </div>

          <div className={styles.fieldItem}>
            <div className={styles.fieldTitle}>
              {t('dataDetail.dataQuality.dimension')}
              <span className={styles.required}>*</span>
            </div>
            <div className={styles.fieldContent}>
              <Radio.Group
                onChange={onChangeFunctionsMap.dimension}
                value={data.dimension}
              >
                {dimensionList.map(i => (
                  <Radio className={styles.centerItem} key={i} value={i}>
                    {t(`dataDetail.dataQuality.dimension.${i}`)}
                  </Radio>
                ))}
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
                <div
                  key={validateRules.key}
                  className={styles.validateFieldContent}
                >
                  <div className={styles.validateRuleIndex}>{index + 1}</div>
                  <div className={styles.validateRuleContainer}>
                    <ValidateRule
                      ref={pushTovalidateRuleRefList}
                      ruleKey={validateRules.key}
                      dimension={data.dimension}
                      defaultRule={validateRules.rule}
                    />
                  </div>

                  <Button
                    style={{ width: 32, padding: 0 }}
                    onClick={() => handleClickDeleteRule(validateRules.key)}
                  >
                    -
                  </Button>
                </div>
              ))}
              <Button onClick={handleClickAddRule}>
                {`+ ${t('dataDetail.dataQuality.validate.add')}`}
              </Button>

              {validateRuleError && (
                <span className={styles.validateRuleError}>
                  {validateRuleError}
                </span>
              )}
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
                dimension={data.dimension}
                selectedTables={data.relatedTables}
                defaultTableId={relatedTable.id}
                onChange={handleChangeRelatedTables}
              />
            </div>
          </div>
        </div>
        <div className={styles.buttonRow}>
          <Button
            size="large"
            style={{ marginRight: 16 }}
            onClick={handleCancel}
          >
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
