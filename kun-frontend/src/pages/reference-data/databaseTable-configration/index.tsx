import React, { useEffect, useState, useMemo, useCallback, useRef } from 'react';
import { Button, Form, message, Modal } from 'antd';
import useRedux from '@/hooks/useRedux';
import {
  ReferenceDataState,
  TableColumn,
  TableRecord,
  TableConfigDetail,
  ShowStatus,
  Column,
  ErrorMessage,
} from '@/definitions/ReferenceData.type';
import {
  editTableConfiguration,
  getTableConfigurationByTableId,
  publish,
  validRdm,
} from '@/services/reference-data/referenceData';
import useUrlState from '@ahooksjs/use-url-state';
import { GlossaryChild } from '@/rematch/models/glossary';
import { Link } from 'umi';
import { KunSpin } from '@/components/KunSpin';
import useI18n from '@/hooks/useI18n';
import { useRequest } from 'ahooks';
import TableConfigration from './components/TableConfigration';
import styles from './index.less';
import UploadModal from '../components/Upload';

const tansTableDataToReq = (tableData: TableRecord[]) => {
  const data = tableData.map((item: TableRecord) => {
    const values: string[] = [];
    Object.keys(item).forEach((key: string) => {
      if (key !== 'recordNumber') {
        values.push(item[key] as string);
      }
    });
    return {
      values,
      recordNumber: item.recordNumber,
    };
  });
  const sortData = data.sort(
    (a: TableRecord, b: TableRecord) => parseInt(a.recordNumber, 10) - parseInt(b.recordNumber, 10),
  );
  return {
    data: sortData,
  };
};

const tansTableColumnsToReq = (tableColumns: TableColumn[]) => {
  const primaryKeys: string[] = [];
  const columns: Column[] = [];
  tableColumns.forEach((item, index) => {
    if (item.dataIndex !== 'recordNumber') {
      if (item.primaryKey) {
        primaryKeys.push(item.name);
      }
      columns.push({
        name: item.name,
        index: index - 1, // 去除recordNumber这一列
        columnType: item.columnType,
        editable: item.editable,
      });
    }
  });
  return {
    columns,
    refTableConstraints: {
      PRIMARY_KEY: primaryKeys,
    },
  };
};

export default function DatabaseTableConfiguration() {
  const t = useI18n();
  const i18n = 'dataDiscovery.referenceData.tableConfig';
  const childrenRef = useRef(null);
  const [routeState, setRouteState] = useUrlState();
  const [form] = Form.useForm();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [errorMessages, setErrorMessages] = useState<ErrorMessage[]>([]);
  const [tableConfigDetail, setTableConfigDetail] = useState<TableConfigDetail>();
  const [glossaryListOptions, setGlossaryListOptions] = useState([]);
  const { selector, dispatch } = useRedux<ReferenceDataState>(state => state.referenceData);
  const { refTableData } = selector;

  const { loading: editLoading, runAsync: saveRun } = useRequest(editTableConfiguration, { manual: true });

  const { loading: publishLoading, runAsync: publishRun } = useRequest(publish, { manual: true });

  const setDetail = useCallback(
    detail => {
      const glossaryList = detail.glossaryList.map((glossary: GlossaryChild) => glossary.id);
      setGlossaryListOptions(detail.glossaryList);
      setTableConfigDetail({
        tableId: detail.tableId,
        databaseName: detail.databaseName,
        tableName: detail.tableName,
        versionDescription: detail.versionDescription,
        ownerList: detail.ownerList || [],
        glossaryList,
        versionId: detail.versionId,
        versionNumber: detail.versionNumber,
        showStatus: detail.showStatus,
        enableEditName: detail.enableEditName,
      });
    },
    [setTableConfigDetail],
  );

  const queryConfigDetail = useCallback(async () => {
    if (routeState.tableId) {
      setLoading(true);
      const res = await getTableConfigurationByTableId(routeState.tableId).catch(() => {
        dispatch.referenceData.updateState({
          key: 'refTableData',
          value: null,
        });
      });
      if (res) {
        setDetail(res);
        dispatch.referenceData.updateState({
          key: 'refTableData',
          value: res.refBaseTable,
        });
      }
      setLoading(false);
    }
  }, [routeState.tableId, dispatch, setDetail]);

  useEffect(() => {
    queryConfigDetail();
  }, [queryConfigDetail]);

  const originColumns = useMemo(() => {
    return refTableData?.refTableMetaData.columns || [];
  }, [refTableData]);

  const originData = useMemo(() => {
    return refTableData?.refData?.data || [];
  }, [refTableData]);

  const primaryKeys = useMemo(() => {
    return refTableData?.refTableMetaData?.refTableConstraints?.PRIMARY_KEY || [];
  }, [refTableData]);

  const saveTable = async () => {
    form.validateFields().then(async values => {
      const refData = tansTableDataToReq(childrenRef?.current?.tableData);
      const refTableMetaData = tansTableColumnsToReq(childrenRef?.current?.tableColumns);

      if (!refTableMetaData.refTableConstraints.PRIMARY_KEY.length) {
        message.error(t('dataDiscovery.referenceData.notice.emptyPrimaryKey'));
        return;
      }
      const params = {
        editRefTableVersionInfo: values,
        refBaseTable: {
          refData,
          refTableMetaData,
        },
        tableId: tableConfigDetail?.tableId,
        versionId: tableConfigDetail?.versionId,
      };
      const res = await saveRun(params);
      if (res) {
        if (res.state) {
          setRouteState({
            tableId: res.baseRefTableVersionInfo.tableId,
          });
          queryConfigDetail();
          message.success(t('dataDiscovery.referenceData.notice.save.success'));
          setErrorMessages([]);
          setIsEditing(false);
        } else {
          message.error(res.validationResultVo.summary);
          setErrorMessages(res.validationResultVo.validationMessageVoList);
        }
      }
    });
  };

  const validateTable = async (newTableData: TableRecord[], column: TableColumn[]) => {
    const refData = tansTableDataToReq(newTableData);
    const refTableMetaData = tansTableColumnsToReq(column);
    const params = {
      refBaseTable: {
        refData,
        refTableMetaData,
      },
    };
    const res = await validRdm(params);
    if (res) {
      if (res.validationMessageVoList) {
        setErrorMessages(res.validationMessageVoList);
      }
      return Promise.resolve();
    }
    return Promise.reject();
  };
  const publishVersion = useCallback(async () => {
    if (tableConfigDetail?.versionId) {
      Modal.confirm({
        title: t('dataDiscovery.referenceData.notice.publish'),
        async onOk() {
          const res = await publishRun(tableConfigDetail.versionId);
          if (res) {
            message.success(t('dataDiscovery.referenceData.notice.publish.success'));
            queryConfigDetail();
          }
        },
      });
    }
  }, [tableConfigDetail?.versionId, queryConfigDetail, t, publishRun]);

  const setEditTableConfig = useCallback(async () => {
    setIsEditing(true);

    form.setFieldsValue(tableConfigDetail);
  }, [tableConfigDetail, form]);

  useEffect(() => {
    if (!routeState.tableId) {
      setEditTableConfig();
    }
  }, [routeState.tableId, setEditTableConfig]);
  return (
    <div className={styles.content}>
      <KunSpin spinning={loading}>
        <div className={styles.header}>
          <div className={styles.title}>
            {t(`${i18n}.title`)}{' '}
            {tableConfigDetail?.versionNumber && (
              <span className={styles.versionNumber}>(V{tableConfigDetail?.versionNumber})</span>
            )}
          </div>
          <div className={styles.buttonGroup}>
            {tableConfigDetail?.tableId && (
              <>
                <Link
                  style={{ marginRight: '16px' }}
                  to={`/data-discovery/reference-data/version?tableId=${tableConfigDetail?.tableId}&tableName=${tableConfigDetail?.tableName}`}
                >
                  {t(`${i18n}.version`)}
                </Link>
                <Button disabled={!isEditing} onClick={() => setIsModalVisible(true)} style={{ marginRight: '16px' }}>
                  {t(`${i18n}.reUpload`)}{' '}
                </Button>
              </>
            )}
            {!isEditing && (
              <Button style={{ marginRight: '16px' }} onClick={() => setEditTableConfig()}>
                {t(`${i18n}.edit`)}
              </Button>
            )}
            {isEditing && (
              <Button style={{ marginRight: '16px' }} loading={editLoading} onClick={saveTable}>
                {t(`${i18n}.save`)}{' '}
              </Button>
            )}
            <Button
              type="primary"
              onClick={publishVersion}
              disabled={!tableConfigDetail?.tableId || tableConfigDetail?.showStatus === ShowStatus.PUBLISHED}
              loading={publishLoading}
            >
              {tableConfigDetail?.showStatus === ShowStatus.PUBLISHED
                ? t('dataDiscovery.referenceData.status.released')
                : t(`${i18n}.publish`)}
            </Button>
          </div>
        </div>
        <div className={styles.con}>
          {refTableData && (
            <TableConfigration
              ref={childrenRef}
              glossaryListOptions={glossaryListOptions}
              form={form}
              tableConfigDetail={tableConfigDetail}
              isEditing={isEditing}
              primaryKeys={primaryKeys}
              originData={originData}
              errorMessages={errorMessages}
              originColumns={originColumns}
              validateTable={validateTable}
            />
          )}
        </div>
        {isModalVisible && (
          <UploadModal
            isModalVisible={isModalVisible}
            reUpload
            versionId={tableConfigDetail?.versionId}
            setIsModalVisible={setIsModalVisible}
          />
        )}
      </KunSpin>
    </div>
  );
}
