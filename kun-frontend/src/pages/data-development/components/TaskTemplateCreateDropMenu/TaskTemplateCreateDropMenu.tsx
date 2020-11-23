import React, { memo, useMemo } from 'react';
import { Button, Dropdown, Menu } from 'antd';
import { DownOutlined } from '@ant-design/icons';
import { useMount } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import { TaskTemplateIcon } from '@/components/TaskTemplateIcon/TaskTemplateIcon.component';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';

import styles from './TaskTemplateCreateDropMenu.module.less';

interface OwnProps {
}

type Props = OwnProps;

export const TaskTemplateCreateDropMenu: React.FC<Props> = memo(function TaskTemplateCreateDropMenu(props) {
  const { dispatch, selector: {
    taskTemplates,
    loading,
  }} = useRedux<{
    taskTemplates: TaskTemplate[],
    loading: boolean;
  }>(s => ({
    taskTemplates: s.dataDevelopment.taskTemplates,
    loading: s.loading.effects.dataDevelopment.fetchTaskTemplates,
  }));

  useMount(() => {
    dispatch.dataDevelopment.fetchTaskTemplates();
  });

  const overlay = useMemo(() => {
    if (taskTemplates && taskTemplates.length) {
      return (
        <Menu>
          {taskTemplates.map(templateItem => (
            <Menu.Item
              key={templateItem.name}
              title={templateItem.name}
            >
              <span>
                <TaskTemplateIcon name={templateItem.name} />
              </span>
              <span>{templateItem.name}</span>
            </Menu.Item>
          ))}
        </Menu>
      );
    }
    // else
    return <></>;
  }, [taskTemplates]);

  return (
    <Dropdown
      className={styles.TaskTemplateCreateDropMenu}
      overlay={overlay}
      placement="bottomRight"
    >
      <Button type="primary" loading={loading}>
        <span>创建任务</span>
        <DownOutlined />
      </Button>
    </Dropdown>
  );
});
