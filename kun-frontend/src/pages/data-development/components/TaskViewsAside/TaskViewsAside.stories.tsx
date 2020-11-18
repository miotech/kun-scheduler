import React, { useState } from 'react';
import { Meta } from '@storybook/react';
import { TaskViewsAside } from '@/pages/data-development/components/TaskViewsAside/TaskViewsAside';
import { IntlProvider } from '@@/core/umiExports';
import { TaskDefinitionViewVO } from '@/definitions/TaskDefinitionView.type';

import '@/global.less';
import { TaskDefViewModificationModal } from '@/pages/data-development/components/TaskDefViewModificationModal/TaskDefViewModificationModal';
import LogUtils from '@/utils/logUtils';
import sleep from '@/utils/sleep';

export default {
  title: 'components/DataDevelopment/TaskViewsAside',
  component: TaskViewsAside,
} as Meta;

const logger = LogUtils.getLoggers('TaskViewAside-StoryBook');

const demoMessage = {
  'dataDevelopment.searchView': 'Search View',
  'dataDevelopment.taskDefView.create': 'Create task definition view',
  'dataDevelopment.taskDefView.edit': 'Edit task definition view',
  'dataDevelopment.taskDefView.name': 'View name',
  'dataDevelopment.taskDefView.name.placeholder': 'Input task definition view name here',
};

const demoViews: TaskDefinitionViewVO[] = [
  {
    id: '108468000000',
    name: 'Task View 1',
    creator: '1',
    includedTaskDefinitionIds: ['1', '2', '3', '4'],
    createTime: '2020-11-16T17:48:39.833+08:00',
    updateTime: '2020-11-16T17:48:39.833+08:00',
  },
  {
    id: '108468000001',
    name: 'Task View 2',
    creator: '1',
    includedTaskDefinitionIds: ['5', '6', '7'],
    createTime: '2020-11-17T12:28:39.833+08:00',
    updateTime: '2020-11-17T12:28:39.833+08:00',
  },
  {
    id: '108468000002',
    name: 'Task View 3 with long long long long long long name',
    creator: '1',
    includedTaskDefinitionIds: ['7', '8'],
    createTime: '2020-11-18T07:48:39.833+08:00',
    updateTime: '2020-11-18T07:48:39.833+08:00',
  },
];

export const Demo = () => {
  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <main
        style={{
          display: 'flex',
          width: '100%',
          height: '100vh',
          flexDirection: 'column',
          border: '1px solid #e0e0e0',
        }}>
        <div style={{ width: '250px', flex: '1 1', position: 'relative' }} data-tid="pseudo-wrapper">
          <TaskViewsAside
            views={demoViews}
          />
        </div>
      </main>
    </IntlProvider>
  );
};

export const DemoWithEditModal = () => {
  const [ createModalVisible, setCreateModalVisible ] = useState<boolean>(false);

  return (
    <IntlProvider locale="en-US" messages={demoMessage}>
      <div>
        <main
          style={{
            display: 'flex',
            width: '100%',
            height: '100vh',
            flexDirection: 'column',
            border: '1px solid #e0e0e0',
          }}>
          <div style={{ width: '250px', flex: '1 1', position: 'relative' }} data-tid="pseudo-wrapper">
            <TaskViewsAside
              views={demoViews}
              onClickCreateBtn={() => {
                setCreateModalVisible(true);
              }}
            />
          </div>
        </main>
        <TaskDefViewModificationModal
          visible={createModalVisible}
          mode="create"
          onOk={async (updateVO) => {
            logger.debug('updateVO = %o', updateVO);
            await sleep(2000);
            setCreateModalVisible(false);
          }}
          onCancel={() => {
            setCreateModalVisible(false);
          }}
        />
      </div>
    </IntlProvider>
  );
};


