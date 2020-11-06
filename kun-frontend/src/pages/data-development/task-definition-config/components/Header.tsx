import React, { useCallback, useState } from 'react';
import moment from 'moment';
import { Space, Button, Descriptions, Modal, message, Tooltip } from 'antd';
import { FormInstance } from 'antd/es/form';
import { history } from 'umi';
import isArray from 'lodash/isArray';
import { EditText } from '@/components/EditText';
import useRedux from '@/hooks/useRedux';
import useI18n from '@/hooks/useI18n';
import LogUtils from '@/utils/logUtils';
import {
  commitAndDeployTaskDefinition, deleteTaskDefinition, updateTaskDefinition
} from '@/services/data-development/task-definitions';

import { getFlattenedTaskDefinition } from '@/utils/transformDataset';
import { TaskCommitModal } from '@/pages/data-development/task-definition-config/components/TaskCommitModal';
import { UserSelect } from '@/components/UserSelect';
import { UsernameText } from '@/components/UsernameText';

import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';

import { transformFormTaskConfig } from '@/pages/data-development/task-definition-config/helpers';

import styles from './Header.less';

export interface Props {
  draftTaskDef: TaskDefinition | null;
  setDraftTaskDef: any;
  form: FormInstance;
  taskDefId: string | number;
  handleCommitDryRun: () => any;
  taskTemplate: TaskTemplate | null;
}

const logger = LogUtils.getLoggers('Header');

export const Header: React.FC<Props> = props => {
  const {
    draftTaskDef,
    setDraftTaskDef,
    form,
    taskDefId,
    handleCommitDryRun,
  } = props;

  const [
    commitModalVisible,
    setCommitModalVisible,
  ] = useState<boolean>(false);

  const t = useI18n();

  const {
    selector: {
      definitionFormDirty,
      backUrl,
    },
    dispatch,
  } = useRedux(s => ({
    definitionFormDirty: s.dataDevelopment.definitionFormDirty,
    backUrl: s.dataDevelopment.backUrl,
  }));

  const handleDeleteBtnClick = useCallback(() => {
    if (draftTaskDef && draftTaskDef.id) {
      Modal.confirm({
        title: t('dataDevelopement.definition.deleteAlert.title'),
        content: t('dataDevelopement.definition.deleteAlert.content'),
        okText: t('common.button.delete'),
        okType: 'danger',
        onOk: () => {
          deleteTaskDefinition(draftTaskDef.id)
            .then(() => {
              if (backUrl) {
                logger.debug('Getting back to url: %o', backUrl);
                history.push(backUrl);
              } else {
                history.push('/data-development');
              }
            })
            .catch(() => {
              message.error('Failed to delete task');
            });
        },
      });
    }
  }, [
    t,
    draftTaskDef,
    backUrl,
  ]);

  const handleSaveBtnClick = useCallback(async () => {
    try {
      logger.debug('Form.values =', form.getFieldsValue());
      await form.validateFields();
      // if all fields are valid
      await updateTaskDefinition({
        id: taskDefId,
        name: draftTaskDef?.name || '',
        owner: draftTaskDef?.owner || '',
        taskPayload: {
          scheduleConfig: {
            ...form.getFieldValue(['taskPayload', 'scheduleConfig']),
            // convert dataset related fields to conform API required shape
            inputDatasets: getFlattenedTaskDefinition(
              form.getFieldValue(['taskPayload', 'scheduleConfig', 'inputDatasets']) || [],
            ),
            inputNodes: (form.getFieldValue(['taskPayload', 'scheduleConfig', 'inputNodes']) || [])
              .map((valueObj: { value: string | number }) => valueObj.value),
          },
          taskConfig: transformFormTaskConfig(form.getFieldValue(['taskPayload', 'taskConfig']), props.taskTemplate),
        },
      });
      message.success(t('common.operateSuccess'));
      dispatch.dataDevelopment.setDefinitionFormDirty(false);
    } catch (e) {
      logger.warn(e);
      // hint each form error
      if (e && e.errorFields && isArray(e.errorFields)) {
        e.errorFields.forEach((fieldErr: { errors: string[] }) => message.error(fieldErr.errors[0]));
      }
    }
  }, [
    form,
    taskDefId, draftTaskDef?.name,
    draftTaskDef?.owner,
    props.taskTemplate,
    t,
    dispatch.dataDevelopment,
  ]);

  const renderCommitBtn = () => {
    if (definitionFormDirty) {
      return (
        <Tooltip title={t('dataDevelopement.definition.commitBtnDisabledTooltip')}>
          <Button type="primary" disabled>
            {t('common.button.commit')}
          </Button>
        </Tooltip>
      )
    }
    // else
    return (
      <Button type="primary" onClick={() => {
        setCommitModalVisible(true);
      }}>
        {t('common.button.commit')}
      </Button>
    );
  };

  return (
    <header className={styles.EditHeader}>
      <div className={styles.TitleAndToolBtnGroup}>
        <h2 className={styles.DefinitionTitle}>
          {/* Definition title and edit input */}
          <EditText
            value={draftTaskDef?.name || ''}
            type="text"
            validation={(value) => {
              return `${value}`.trim().length !== 0;
            }}
            onSave={(value: string) => {
              dispatch.dataDevelopment.setDefinitionFormDirty(true);
              setDraftTaskDef({
                ...draftTaskDef,
                name: value,
              })
            }}
          />
        </h2>
        {/* Tool buttons */}
        <div className={styles.HeadingButtons}>
          <Space>
            {/* Delete */}
            <Button onClick={handleDeleteBtnClick}>
              {t('common.button.delete')}
            </Button>
            {/* Dry run */}
            <Button onClick={handleCommitDryRun}>
              {t('common.button.dryrun')}
            </Button>
            {/* Save */}
            <Button onClick={handleSaveBtnClick}>
              {t('common.button.save')}
            </Button>
            {/* Commit */}
            {renderCommitBtn()}
          </Space>
        </div>
      </div>
      <div className={styles.TaskDefMetas}>
        <Descriptions column={2}>
          {/* Owner */}
          <Descriptions.Item label={t('dataDevelopment.definition.property.owner')}>
            {
              draftTaskDef?.owner ? (
                <UserSelect
                  style={{ width: '200px' }}
                  value={`${draftTaskDef.owner}`}
                  onChange={(nextUserValue: string | string[]) => {
                    if (typeof nextUserValue !== 'string') {
                      return;
                    }
                    setDraftTaskDef({
                      ...draftTaskDef,
                      owner: nextUserValue,
                    });
                  }}
                />
              ) : '...'}
          </Descriptions.Item>
          {/* Last modifier */}
          <Descriptions.Item label={t('dataDevelopment.definition.property.updater')}>
            {draftTaskDef?.lastModifier ? <UsernameText userId={draftTaskDef?.lastModifier} /> : '...'}
          </Descriptions.Item>
          {/* Create time */}
          <Descriptions.Item label={t('dataDevelopment.definition.property.createTime')}>
            {draftTaskDef?.createTime ? moment(draftTaskDef.createTime).format('YYYY-MM-DD HH:mm:ss') : '...'}
          </Descriptions.Item>
          {/* Last update time */}
          <Descriptions.Item label={t('dataDevelopment.definition.property.lastUpdateTime')}>
            {draftTaskDef?.lastUpdateTime ? moment(draftTaskDef.lastUpdateTime).format('YYYY-MM-DD HH:mm:ss') : '...'}
          </Descriptions.Item>
        </Descriptions>
      </div>
      {/* Commit confirm modal */}
      <TaskCommitModal
        visible={commitModalVisible}
        onCancel={() => {
          setCommitModalVisible(false);
        }}
        onConfirm={async (commitMsg: string) => {
         const respData = await commitAndDeployTaskDefinition(taskDefId, commitMsg);
         if (respData) {
           message.success('Commit success.');
           setCommitModalVisible(false);
         }
        }}
      />
    </header>
  );
};
