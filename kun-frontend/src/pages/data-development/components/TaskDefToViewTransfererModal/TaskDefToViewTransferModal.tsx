import React, { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { Col, Modal, Row, Select, Transfer } from 'antd';
import find from 'lodash/find';
import uniqBy from 'lodash/uniqBy';
import { useRequest } from 'ahooks';
import useI18n from '@/hooks/useI18n';
import { KunSpin } from '@/components/KunSpin';
import LogUtils from '@/utils/logUtils';

import { fetchTaskDefinitionViewDetail } from '@/services/data-development/task-definition-views';
import { fetchAllTaskDefinitions } from '@/services/data-development/task-definitions';

import { TaskDefinitionViewBase } from '@/definitions/TaskDefinitionView.type';
import { TaskDefinition, TaskDefinitionModel } from '@/definitions/TaskDefinition.type';


interface OwnProps {
  visible?: boolean;
  viewsList: TaskDefinitionViewBase[];
  initSrcView?: TaskDefinitionViewBase | null;
  lockSrcView?: boolean;
  initTargetView?: TaskDefinitionViewBase | null;
  lockTargetView?: boolean;
  onCancel?: () => any;
}

type Props = OwnProps;

export const logger = LogUtils.getLoggers('TaskDefToViewTransferModal');

export const TaskDefToViewTransferModal: React.FC<Props> = memo(function TaskDefToViewTransferModal(props) {
  const {
    visible,
    initSrcView,
    initTargetView,
    viewsList,
  } = props;

  const t = useI18n();

  const [ srcView, setSrcView ] = useState<TaskDefinitionViewBase | null>(initSrcView || null);
  const [ targetView, setTargetView ] = useState<TaskDefinitionViewBase | null>(initTargetView || null);
  const [ loadingSrcView, setLoadingSrcView ] = useState<boolean>(false);
  const [ taskDefsOfSrcView, setTaskDefsOfSrcView ] = useState<(TaskDefinition | TaskDefinitionModel)[]>([]);
  const [ draftTargetViewTaskDefinitions, setDraftTargetViewTaskDefinitions ] = useState<(TaskDefinition | TaskDefinitionModel)[]>([]);
  const [ draftTargetViewDefinitionIds, setDraftTargetViewDefinitionIds ] = useState<string[]>([]);

  const {
    loading: loadingTargetView,
    run: doTargetViewFetch,
  } = useRequest(fetchTaskDefinitionViewDetail, {
    manual: true,
  });

  useEffect(() => {
    setSrcView(initSrcView || null);
  }, [
    initSrcView,
  ]);

  useEffect(() => {
    setTargetView(initTargetView || null);
  }, [
    initTargetView,
  ]);

  useEffect(() => {
    setLoadingSrcView(true);
    if (srcView) {
      fetchTaskDefinitionViewDetail(srcView.id)
        .then(data => {
          if (data) {
            setTaskDefsOfSrcView(data.includedTaskDefinitions.map(taskDef => ({
              ...taskDef,
              id: taskDef.definitionId,
            })));
          }
        })
        .finally(() => {
          setLoadingSrcView(false);
        });
    } else {
      // fetch all task definitions
      fetchAllTaskDefinitions().then(data => {
        if (data) {
          setTaskDefsOfSrcView(data);
        }
      }).finally(() => {
        setLoadingSrcView(false);
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    srcView,
  ]);

  useEffect(() => {
    if (targetView) {
      doTargetViewFetch(targetView.id).then(data => {
        if (data) {
          // Reset draft selected keys when reloaded
          setDraftTargetViewDefinitionIds((data?.includedTaskDefinitions || []).map(taskDef => `${taskDef.definitionId}`));
          setDraftTargetViewTaskDefinitions((data?.includedTaskDefinitions || []).map(taskdef => ({
            ...taskdef,
            id: taskdef.definitionId,
          })));
        }
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    targetView,
  ]);

  const srcViewSelect = useMemo(() => {
    return (
      <Select
        className="full-width"
        data-tid="src-view-select"
        placeholder="(All tasks)"
        showSearch
        optionFilterProp="title"
        value={srcView?.id}
        disabled={props.lockSrcView}
        allowClear
        onChange={(viewId: string) => {
          const nextViewState = find(viewsList, view => view.id === viewId);
          setSrcView(nextViewState || null);
        }}
      >
        {
          (viewsList || []).map(view => (
            <Select.Option
              key={view.id}
              value={view.id}
              title={view.name}
            >
              {view.name}
            </Select.Option>
          ))
        }
      </Select>
    );
  }, [
    props.lockSrcView,
    viewsList,
    srcView,
  ]);

  const targetViewSelect = useMemo(() => {
    return (
      <Select
        className="full-width"
        data-tid="src-view-select"
        showSearch
        optionFilterProp="title"
        value={targetView?.id}
        disabled={props.lockTargetView}
        onChange={(viewId: string) => {
          const nextViewState = find(viewsList, view => view.id === viewId);
          setTargetView(nextViewState || null);
        }}
      >
        {
          (viewsList || []).map(view => (
            <Select.Option
              key={view.id}
              value={view.id}
              title={view.name}
            >
              {view.name}
            </Select.Option>
          ))
        }
      </Select>
    );
  }, [
    props.lockTargetView,
    targetView,
    viewsList,
  ]);

  const dataSource = useMemo(() => {
    const mergedDataSource = [
      ...taskDefsOfSrcView,
      ...draftTargetViewTaskDefinitions,
    ];
    return uniqBy(mergedDataSource, item => `${item.id}`);
  }, [
    taskDefsOfSrcView,
    draftTargetViewTaskDefinitions,
  ]);

  const renderItem = useCallback((taskDefItem: TaskDefinition | TaskDefinitionModel) => {
    return {
      label: <span>{taskDefItem.name}</span>,
      title: taskDefItem.name,
      key: taskDefItem.id,
    };
  }, []);

  const handleChange = useCallback(function handleChange(targetKeys: string[] /* , direction: TransferDirection, moveKeys: string[] */) {
    setDraftTargetViewDefinitionIds(targetKeys);
    setDraftTargetViewTaskDefinitions([...dataSource].filter(taskDef => targetKeys.indexOf(`${taskDef.id}`) >= 0));
  }, [
    dataSource
  ]);


  return (
    <Modal
      title="Add tasks to view"
      visible={visible}
      width={800}
      onCancel={props.onCancel}
      destroyOnClose
    >
      <Row style={{ width: '100%', marginBottom: '16px' }}>
        <Col flex="none" style={{ width: '350px' }}>
          {srcViewSelect}
        </Col>
        <Col flex="0 0 42px" />
        <Col flex="none" style={{ width: '350px' }}>
          {targetViewSelect}
        </Col>
      </Row>
      <KunSpin spinning={loadingSrcView || loadingTargetView}>
        <Row>
          <Transfer
            titles={[
              t('dataDevelopment.transferModal.srcTitle'),
              t('dataDevelopment.transferModal.targetTitle'),
            ]}
            listStyle={{
              width: 350,
              height: 450,
            }}
            oneWay
            dataSource={dataSource || []}
            targetKeys={draftTargetViewDefinitionIds}
            rowKey={taskDefItem => `${taskDefItem.id}`}
            onChange={handleChange}
            render={renderItem as any}
          />
        </Row>
      </KunSpin>
    </Modal>
  );
});
