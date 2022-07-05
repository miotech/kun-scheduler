import React, { useState } from 'react';
import { Modal, Upload, message } from 'antd';
import { CloudUploadOutlined } from '@ant-design/icons';
import { UploadProps } from 'antd/es/upload/interface';
import useRedux from '@/hooks/useRedux';
import { history } from 'umi';
import { ReferenceDataState } from '@/definitions/ReferenceData.type';
import useI18n from '@/hooks/useI18n';

const { Dragger } = Upload;

interface Props {
  isModalVisible: boolean;
  setIsModalVisible: (value: boolean) => void;
  reUpload?: boolean;
}
const UploadExcel: React.FC<Props> = (props: Props) => {
  const { dispatch } = useRedux(() => {});
  const [referenceData, setReferenceData] = useState<null | ReferenceDataState>();
  const { isModalVisible, reUpload, setIsModalVisible } = props;
  const t = useI18n();
  const i18n = 'dataDiscovery.referenceData.uploadExcel';
  const handleUpload = async () => {
    dispatch.referenceData.updateState({
      key: 'refTableData',
      value: referenceData,
    });
    if (reUpload) {
      setIsModalVisible(false);
    } else {
      history.push('/data-discovery/reference-data/table-configration');
    }
  };

  const uploadProps: UploadProps = {
    name: 'file',
    accept: '.xlsx, .xls, .csv',
    multiple: false,
    maxCount: 1,
    action: '/kun/api/v1/rdm/data/parse',
    onRemove: () => {
      setReferenceData(null);
    },
    onChange: info => {
      const { status, response } = info.file;
      if (status === 'done') {
        setReferenceData(response.result);
      } else if (status === 'error') {
        setReferenceData(null);
        message.error(response.note);
      }
    },
  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  return (
    <Modal
      title={t(i18n)}
      visible={isModalVisible}
      okButtonProps={{ disabled: !referenceData }}
      onOk={handleUpload}
      onCancel={handleCancel}
    >
      <Dragger {...uploadProps}>
        <p className="ant-upload-drag-icon">
          <CloudUploadOutlined />
        </p>
        <p className="ant-upload-text">{t(`${i18n}.content`)}</p>
        <p className="ant-upload-hint">{t(`${i18n}.description`)}</p>
      </Dragger>
    </Modal>
  );
};

export default UploadExcel;
