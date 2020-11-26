import React, { memo } from 'react';
import Icon, { CodeOutlined, ConsoleSqlOutlined, SettingOutlined } from '@ant-design/icons';

import { ReactComponent as SparkIcon } from '@/assets/icons/apachespark.svg';
import { ReactComponent as SparkSQLIcon } from '@/assets/icons/sparksql.svg';
import { ReactComponent as SheetFileIcon } from '@/assets/icons/Sheet-File.svg';
import { ReactComponent as DataSyncIcon } from '@/assets/icons/datasync.svg';
import { ReactComponent as ExportIcon } from '@/assets/icons/export.svg';

interface OwnProps {
  name?: string;
}

type Props = OwnProps;

export const TaskTemplateIcon: React.FC<Props> = memo(function TaskTemplateIcon({ name }) {
  if (!name) {
    return <SettingOutlined />;
  }
  // spark sql
  if (name.match(/sparksql/i)) {
    return <Icon component={SparkSQLIcon} />;
  }
  // spark
  if (name.match(/spark/i)) {
    return <Icon component={SparkIcon} />;
  }
  // sql
  if (name.match(/sql/i)) {
    return <ConsoleSqlOutlined />;
  }
  if (name.match(/bash/i)) {
    return <CodeOutlined />;
  }
  // sheet
  if (name.match(/sheet/i)) {
    return <Icon component={SheetFileIcon} />;
  }
  // datasync
  if (name.match(/datasync/i)) {
    return <Icon component={DataSyncIcon} />;
  }
  // export
  if (name.match(/export/i)) {
    return <Icon component={ExportIcon} />;
  }
  return <SettingOutlined />;
});
