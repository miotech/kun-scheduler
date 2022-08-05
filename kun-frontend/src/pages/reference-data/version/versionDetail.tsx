import React, { memo, useEffect, useState, useCallback, useMemo } from 'react';
import { Drawer } from 'antd';
import { ReferenceDataState, TableConfigDetail } from '@/definitions/ReferenceData.type';
import { GlossaryChild } from '@/rematch/models/glossary';
import useRedux from '@/hooks/useRedux';
import { getTableConfigurationByVersionId } from '@/services/reference-data/referenceData';
import { KunSpin } from '@/components/KunSpin';
import TableConfigration from '../databaseTable-configration/components/TableConfigration';

interface Props {
  versionId?: string;
  setVersionId: (value: string) => void;
}
const VersionDetail = memo((props: Props) => {
  const { versionId, setVersionId } = props;
  const [tableConfigDetail, setTableConfigDetail] = useState<TableConfigDetail>();
  const [glossaryListOptions, setGlossaryListOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const { selector, dispatch } = useRedux<ReferenceDataState>(state => state.referenceData);
  const { refTableData } = selector;

  const setDetail = useCallback(
    detail => {
      const glossaryList = detail.glossaryList.map((glossary: GlossaryChild) => glossary.id);
      setGlossaryListOptions(detail.glossaryList);
      setTableConfigDetail({
        tableId: detail.tableId,
        tableName: detail.tableName,
        databaseName: detail.databaseName,
        versionDescription: detail.versionDescription,
        ownerList: detail.ownerList,
        glossaryList,
        versionId: detail.versionId,
        showStatus:detail.showStatus,
        enableEditName: detail.enableEditName,
      });
    },
    [setTableConfigDetail],
  );

  useEffect(() => {
    const queryTableConfiguration = async () => {
      setLoading(true);
      const res = await getTableConfigurationByVersionId(versionId as string).catch(() => {
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
    };
    if (versionId) {
      queryTableConfiguration();
    }
  }, [versionId, dispatch, setDetail]);

  const originColumns = useMemo(() => {
    return refTableData?.refTableMetaData.columns || [];
  }, [refTableData]);

  const originData = useMemo(() => {
    return refTableData?.refData?.data || [];
  }, [refTableData]);

  const primaryKeys = useMemo(() => {
    return refTableData?.refTableMetaData?.refTableConstraints?.PRIMARY_KEY || [];
  }, [refTableData]);
  useEffect(() => {}, [versionId]);
  return (
    <Drawer
      title={tableConfigDetail?.tableName}
      width="80%"
      placement="right"
      onClose={() => setVersionId('')}
      visible={!!versionId}
    >
      <KunSpin spinning={loading}>
        <TableConfigration
          glossaryListOptions={glossaryListOptions}
          tableConfigDetail={tableConfigDetail}
          isEditing={false}
          primaryKeys={primaryKeys}
          originData={originData}
          originColumns={originColumns}
        />
      </KunSpin>
    </Drawer>
  );
});
export default VersionDetail;
