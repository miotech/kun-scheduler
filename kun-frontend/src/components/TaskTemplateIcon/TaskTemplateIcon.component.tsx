import React, { memo } from 'react';
import Icon, { CodeOutlined, ConsoleSqlOutlined, SettingOutlined } from '@ant-design/icons';

import { ReactComponent as SparkIcon } from '@/assets/icons/apachespark.svg';
import { ReactComponent as SparkSQLIcon } from '@/assets/icons/sparksql.svg';
import { ReactComponent as SheetFileIcon } from '@/assets/icons/Sheet-File.svg';
import { ReactComponent as DataSyncIcon } from '@/assets/icons/datasync.svg';
import { ReactComponent as ExportIcon } from '@/assets/icons/export.svg';

interface OwnProps {
  name?: string;
  className?: string;
}

type Props = OwnProps;

export const TaskTemplateIcon: React.FC<Props> = memo(function TaskTemplateIcon({ name, className }) {
  if (!name) {
    return <SettingOutlined className={className} />;
  }
  // spark sql
  if (name.match(/sparksql/i)) {
    return <Icon className={className} component={SparkSQLIcon} />;
  }
  // spark
  if (name.match(/spark/i)) {
    return <Icon className={className} component={SparkIcon} />;
  }
  // sql
  if (name.match(/sql/i)) {
    return <ConsoleSqlOutlined className={className} />;
  }
  if (name.match(/bash/i)) {
    return <CodeOutlined className={className} />;
  }
  // sheet
  if (name.match(/sheet/i)) {
    return <Icon className={className} component={SheetFileIcon} />;
  }
  // datasync
  if (name.match(/datasync/i)) {
    return <Icon className={className} component={DataSyncIcon} />;
  }
  // export
  if (name.match(/export/i)) {
    return <Icon className={className} component={ExportIcon} />;
  }
  return <SettingOutlined className={className} />;
});
