import React, { memo, useEffect, useMemo, useState } from 'react';
import { Col, Modal, Row, Select, Transfer } from 'antd';
import find from 'lodash/find';
import { TaskDefinitionViewBase } from '@/definitions/TaskDefinitionView.type';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { useRequest } from 'ahooks';
import { fetchTaskDefinitionViewDetail } from '@/services/data-development/task-definition-views';

interface OwnProps {
  visible?: boolean;
  viewsList: TaskDefinitionViewBase[];
  initSrcView?: TaskDefinitionViewBase | null;
  lockSrcView?: boolean;
  initTargetView?: TaskDefinitionViewBase | null;
  lockTargetView?: boolean;
}

type Props = OwnProps;

export const TaskDefToViewTransferModal: React.FC<Props> = memo(function TaskDefToViewTransfererModal(props) {
  const {
    visible,
    initSrcView,
    initTargetView,
    viewsList,
  } = props;

  const [ srcView, setSrcView ] = useState<TaskDefinitionViewBase | null>(initSrcView || null);
  const [ targetView, setTargetView ] = useState<TaskDefinitionViewBase | null>(initTargetView || null);
  const [ taskDefsOfSrcView, setTaskDefsOfSrcView ] = useState<TaskDefinition[]>([]);
  const [ taskDefsOfTargetView, setTaskDefsOfTargetView ] = useState<TaskDefinition[]>([]);

  const {
    data: srcViewData,
    loading: loadingSrcView,
    run: doSrcViewFetch,
  } = useRequest(fetchTaskDefinitionViewDetail, {
    manual: true,
  });
  const {
    data: targetViewData,
    loading: loadingTargetView,
    run: doTargetViewFetch,
  } = useRequest(fetchTaskDefinitionViewDetail, {
    manual: true,
  });

  useEffect(() => {}, [
    srcView
  ]);

  const srcViewSelect = useMemo(() => {
    return (
      <Select
        data-tid="src-view-select"
        showSearch
        optionFilterProp="title"
        value={srcView?.id}
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
    viewsList,
    srcView,
  ]);

  const targetViewSelect = useMemo(() => {
    return (
      <Select
        data-tid="src-view-select"
        showSearch
        optionFilterProp="title"
        value={targetView?.id}
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
    targetView,
    viewsList,
  ]);

  return (
    <Modal
      title="Add tasks to view"
      visible={visible}
    >
      <Row>
        <Col span={12}>
          {srcViewSelect}
        </Col>
        <Col span={12}>
          {targetViewSelect}
        </Col>
      </Row>
      <Transfer

      />
    </Modal>
  );
});
