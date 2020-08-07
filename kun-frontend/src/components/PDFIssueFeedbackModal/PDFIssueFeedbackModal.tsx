import React, { memo, useState } from 'react';
import { Modal, Form, Input } from 'antd';
import useI18n from '@/hooks/useI18n';

interface OwnProps {
  visible?: boolean;
  onOk?: (companyName: string, pdfUrl: string) => any;
  onCancel?: () => any;
}

type Props = OwnProps;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 20 },
  },
};

const formInitialValues = {
  companyName: '',
  pdfUrl: '',
};

export const PDFIssueFeedbackModal: React.FC<Props> = memo((props) => {
  const { visible, onOk, onCancel } = props;
  const [ form ] = Form.useForm();
  const [ sendingReq, setSendingReq ] = useState<boolean>(false);
  const t = useI18n();

  /*
  const pdfListFields = useMemo(() => {
    return (
      <Form.List name="pdfUrls">
        {(fields, { add, remove }) => {
          return (
            <div data-tid="pdf-url-fields">
              {fields.map((field, index) => {
                return (
                  <div>
                    <Form.Item
                      {...((index === 0) ? formItemLayout : formItemLayoutWithOutLabel)}
                      label={(index === 0) ? t('pdfIssueFeedback.PDFUrl') : ''}
                      required={false}
                      key={field.key}
                    >
                      <Input style={{ width: 'calc(100% - 60px)' }} />
                      {
                        (fields.length > 1) ? (
                          <Button
                            type="link"
                            icon={<CloseOutlined />}
                            onClick={() => { remove(field.name); }}
                          />
                        ) : null
                      }
                    </Form.Item>
                  </div>
                );
              })}
              <Row>
                <Col offset={4} span={20}>
                  <Button
                    type="dashed"
                    onClick={() => { add(); }}
                    style={{ width: 'calc(100% - 60px)' }}
                    block
                  >
                    <PlusOutlined />
                    <span>{t('common.button.add')}</span>
                  </Button>
                </Col>
              </Row>
            </div>
          );
        }}
      </Form.List>
    );
  }, [
    t
  ]);
  */

  return (
    <Modal
      title={t('pdfIssueFeedback.title')}
      visible={visible}
      width={800}
      onOk={async () => {
        try {
          await form.validateFields();
          setSendingReq(true);
          if (onOk) {
            await onOk(
              form.getFieldValue('companyName'),
              form.getFieldValue('pdfUrl'),
            );
            // if success
            form.resetFields();
          }
        } catch (e) {
          // Do nothing
        } finally {
          setSendingReq(false);
        }
      }}
      onCancel={onCancel}
      cancelText={t('common.button.cancel')}
      okText={t('common.button.confirm')}
      okButtonProps={{
        loading: sendingReq,
      }}
    >
      <Form form={form} {...formItemLayout} initialValues={formInitialValues}>
        <Form.Item
          label={t('pdfIssueFeedback.companyName')}
          name="companyName"
          rules={[ { required: true } ]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          required
          label={t('pdfIssueFeedback.PDFUrl')}
          rules={[ { required: true } ]}
          name="pdfUrl"
        >
          <Input.TextArea
            rows={4}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
});
