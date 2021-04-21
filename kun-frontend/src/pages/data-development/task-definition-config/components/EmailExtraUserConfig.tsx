import React, { memo } from 'react';
import { FormInstance } from 'antd/es/form';
import { Button, Col, Divider, Form, Input, Row } from 'antd';
import useI18n from '@/hooks/useI18n';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';

interface OwnProps {
  form: FormInstance;
  emailUserConfigIndex: number;
}

type Props = OwnProps;

const formItemLayoutWithOutLabel = {
  wrapperCol: {
    sm: { span: 24, offset: 0 },
  },
};

export const EmailExtraUserConfig: React.FC<Props> = memo(function EmailExtraUserConfig(props) {
  const { emailUserConfigIndex } = props;

  const t = useI18n();

  if (emailUserConfigIndex < 0) {
    return <React.Fragment />;
  }

  return (
    <div>
      <Divider />
      <Row>
        <Col span={6}>
          <label style={{ color: '#526079' }} htmlFor="#">
            {t('dataDevelopment.definition.notificationConfig.EMAIL.emailList')}：
          </label>
        </Col>
        <Col span={18}>
          <Form.List name={['taskPayload', 'notifyConfig', 'notifierConfig', emailUserConfigIndex, 'emailList']}>
            {(fields, { add, remove }, { errors }) => (
              <React.Fragment>
                {fields.map(field => {
                  // noinspection RequiredAttributes
                  return (
                    <Form.Item {...formItemLayoutWithOutLabel} key={field.key} required={false}>
                      <Row>
                        <Col flex="1 1">
                          <Form.Item
                            {...field}
                            validateTrigger={['onChange', 'onBlur']}
                            rules={[
                              {
                                type: 'email',
                                message: t(
                                  'dataDevelopment.definition.notificationConfig.EMAIL.emailList.validationError',
                                ),
                              },
                            ]}
                          >
                            <Input type="email" />
                          </Form.Item>
                        </Col>
                        <Col flex="0 0 20px">
                          <Button type="link" icon={<MinusCircleOutlined />} onClick={() => remove(field.name)} />
                        </Col>
                      </Row>
                    </Form.Item>
                  );
                })}
                <Row>
                  <Col span={24}>
                    <Button type="dashed" onClick={() => add()} style={{ width: '100%' }} icon={<PlusOutlined />}>
                      Add Email Address
                    </Button>
                  </Col>
                </Row>
                <Form.ErrorList errors={errors} />
              </React.Fragment>
            )}
          </Form.List>
        </Col>
      </Row>
    </div>
  );
});
