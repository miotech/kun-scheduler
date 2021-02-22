import React, { memo } from 'react';
import { InfoCircleOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';
import useI18n from '@/hooks/useI18n';

interface OwnProps {}

type Props = OwnProps;

export const SecretHintIcon: React.FC<Props> = memo(function SecretHintIcon() {
  const t = useI18n();

  return (
    <Tooltip title={t('settings.variableSettings.isSecret.hint')}>
      <InfoCircleOutlined />
    </Tooltip>
  );
});
