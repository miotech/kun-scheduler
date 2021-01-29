import Icon, {
  ApartmentOutlined,
  CalendarOutlined,
  FilePdfOutlined,
  FileTextOutlined,
  LineChartOutlined,
  SettingOutlined,
  SnippetsOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import { ReactComponent as TaskScheduledIcon } from '@/assets/icons/task-scheduled.svg';
import { ReactComponent as TaskInstantIcon } from '@/assets/icons/task-instant.svg';
import { ReactComponent as DataDevelopmentIcon } from '@/assets/icons/data-develop.svg';
import { ReactComponent as OperationCenterIcon } from '@/assets/icons/operation-center-view.svg';
import { ReactComponent as DataDiscoveryIcon } from '@/assets/icons/data-discovery.svg';

import React from 'react';

const TaskScheduled = <Icon component={TaskScheduledIcon} />;
const TaskInstant = <Icon component={TaskInstantIcon} />;
const DataDevelopment = <Icon component={DataDevelopmentIcon} />;
const OperationCenter = <Icon component={OperationCenterIcon} />;
const DataDiscovery = <Icon component={DataDiscoveryIcon} />;

export interface IconComponentMap {
  [key: string]: React.ReactNode;
}

export const iconComponentMap: IconComponentMap = {
  FileTextOutlined: <FileTextOutlined />,
  SettingOutlined: <SettingOutlined />,
  SnippetsOutlined: <SnippetsOutlined />,
  FilePdfOutlined: <FilePdfOutlined />,
  CalendarOutlined: <CalendarOutlined />,
  ToolOutlined: <ToolOutlined />,
  ApartmentOutlined: <ApartmentOutlined />,
  LineChartOutlined: <LineChartOutlined />,
  TaskScheduled,
  TaskInstant,
  DataDevelopment,
  OperationCenter,
  DataDiscovery,
};
