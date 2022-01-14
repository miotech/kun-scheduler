import { useHistory } from 'umi';
import React, { memo, useMemo, useState, useCallback } from 'react';
import { Button, Dropdown, Menu, notification, Input } from 'antd';
import { DownOutlined, PlusOutlined } from '@ant-design/icons';
import { useMount } from 'ahooks';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';

import { TaskTemplateIcon } from '@/components/TaskTemplateIcon/TaskTemplateIcon.component';
import { TaskDefinitionCreationModal } from '@/pages/data-development/components/TaskTemplateCreateDropMenu/TaskDefinitionCreationModal';

import { TaskTemplate } from '@/definitions/TaskTemplate.type';

import styles from './TaskTemplateCreateDropMenu.module.less';

interface OwnProps {
  onCreateTaskDefinition: (taskTemplateName: string, name: string, createInCurrentView: boolean) => any;
}

type Props = OwnProps;

export const TaskTemplateCreateDropMenu: React.FC<Props> = memo(function TaskTemplateCreateDropMenu(props) {
  const {
    dispatch,
    selector: { taskTemplates, loading },
  } = useRedux<{
    taskTemplates: TaskTemplate[];
    loading: boolean;
  }>(s => ({
    taskTemplates: s.dataDevelopment.taskTemplates,
    loading: s.loading.effects.dataDevelopment.fetchTaskTemplates,
  }));

  const history = useHistory();

  const t = useI18n();

  const [selectedTemplateName, setSelectedTemplateName] = useState<string | null>(null);
  const [templateSearchValue, setTemplateSearchValue] = useState<string | null>(null);

  useMount(() => {
    dispatch.dataDevelopment.fetchTaskTemplates();
  });
  const onChange = (e: any) => {
    const { value } = e.target;
    setTemplateSearchValue(value);
  };
  const overlay = useMemo(() => {
    if (taskTemplates && taskTemplates.length) {
      return (
        <Menu
          className={styles.menu}
          onClick={({ key }) => {
            setSelectedTemplateName(key as string);
          }}
        >
          <div style={{ padding: '7px' }}>
            <Input onChange={onChange} placeholder="search" allowClear />
          </div>
          {taskTemplates.map(templateItem => {
            const rep = templateSearchValue
              ? templateItem.name.toLowerCase().includes(templateSearchValue as string)
              : true;
            if (rep) {
              return (
                <Menu.Item key={templateItem.name} title={templateItem.name}>
                  <span>
                    <TaskTemplateIcon name={templateItem.name} />
                    &nbsp;
                  </span>
                  <span>{templateItem.name}</span>
                </Menu.Item>
              );
            }
            return '';
          })}
        </Menu>
      );
    }
    // else
    return <></>;
  }, [taskTemplates, templateSearchValue]);

  const handleClickTask = useCallback(
    (id, e) => {
      e.preventDefault();
      history.push(`/data-development/task-definition/${id}`);
    },
    [history],
  );

  const handleOk = useCallback(
    async (name1: string, createInCurrentView: boolean) => {
      try {
        const resp = await props.onCreateTaskDefinition(selectedTemplateName as string, name1, createInCurrentView);
        const { id, name } = resp;
        // history.push(`/data-development/task-definition/${id}`);

        notification.open({
          message: t('taskTemplate.create.notification.title'),
          description: (
            <span>
              {t('taskTemplate.create.notification.desc')}{' '}
              <a href={`/data-development/task-definition/${id}`} onClick={e => handleClickTask(id, e)}>
                {name}
              </a>
            </span>
          ),
        });

        setSelectedTemplateName(null);
      } catch (e) {
        // do nothing
      }
    },
    [handleClickTask, props, selectedTemplateName, t],
  );

  return (
    <>
      <Dropdown className={styles.TaskTemplateCreateDropMenu} overlay={overlay} placement="bottomRight">
        <Button type="primary" loading={loading} icon={<PlusOutlined />}>
          <span>{t('dataDevelopment.definition.creationTitle')}</span>
          <DownOutlined />
        </Button>
      </Dropdown>
      <TaskDefinitionCreationModal
        taskTemplateName={selectedTemplateName || undefined}
        visible={selectedTemplateName != null}
        onOk={handleOk}
        onCancel={() => {
          setSelectedTemplateName(null);
        }}
      />
    </>
  );
});
