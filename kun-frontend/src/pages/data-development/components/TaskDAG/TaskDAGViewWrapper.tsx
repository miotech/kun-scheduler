import React, { memo, useEffect, useMemo, useState } from 'react';
import find from 'lodash/find';
import { DataDevelopmentModelFilter } from '@/rematch/models/dataDevelopment/model-state';
import { fetchAllTaskDefinitions } from '@/services/data-development/task-definitions';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { fetchAllTaskDefinitionsByViewId } from '@/services/data-development/task-definition-views';
import { TaskDAG } from '@/pages/data-development/components/TaskDAG/TaskDAG';
import { DAGNodeInfoDrawer } from '@/pages/data-development/components/DAGNodeInfoDrawer/DAGNodeInfoDrawer';
import { Button } from 'antd';
import { KunSpin } from '@/components/KunSpin';

interface OwnProps {
  taskDefViewId: string | null;
  filters: DataDevelopmentModelFilter;
  /** Triggers update when changed */
  updateTime: number;
  selectedTaskDefIds: string[];
  setSelectedTaskDefIds?: (taskDefIds: string[]) => any;
  setAddToOtherViewModalVisible?: any;
}

type Props = OwnProps;

export const TaskDAGViewWrapper: React.FC<Props> = memo(function TaskDAGViewWrapper(props) {
  const {
    taskDefViewId,
    // filters,
    // updateTime,
    selectedTaskDefIds,
    setSelectedTaskDefIds,
  } = props;

  const containerRef = React.useRef() as any;

  const [ isLoading, setIsLoading ] = useState<boolean>(false);
  const [ taskDefinitions, setTaskDefinitions ] = useState<TaskDefinition[]>([]);
  const [ viewportResetHookValue, setViewportResetHookValue ] = useState<number>(Date.now());

  useEffect(() => {
    setIsLoading(true);
    if (taskDefViewId == null) {
      fetchAllTaskDefinitions()
        .then(taskDefPayloads => {
          if (taskDefPayloads) {
            setTaskDefinitions(taskDefPayloads);
            setViewportResetHookValue(Date.now());
          }
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      fetchAllTaskDefinitionsByViewId(taskDefViewId)
        .then(taskDefPayloads => {
          if (taskDefPayloads) {
            setTaskDefinitions(taskDefPayloads);
            setViewportResetHookValue(Date.now());
          }
        })
        .finally(() => {
          setIsLoading(false);
        });
    }
  }, [
    taskDefViewId,
  ]);

  const drawerVisible =  selectedTaskDefIds.length === 1;

  const drawerTaskDef = useMemo(() => (
    selectedTaskDefIds.length >= 1 ?
      find(taskDefinitions, taskDef => taskDef.id === selectedTaskDefIds[0]) :
      null
  ), [selectedTaskDefIds, taskDefinitions]);

  const renderTools = () => {
    if (!selectedTaskDefIds.length) {
      return <></>;
    }
    // else
    return (
      <Button onClick={() => {
        if (props.setAddToOtherViewModalVisible) {
          props.setAddToOtherViewModalVisible(true);
        }
      }}>
        Add to other views ({selectedTaskDefIds.length} Items)
      </Button>
    );
  };

  return (
    <div
      id="app-task-dag-outer-container"
      style={{ position: 'relative', width: '100%', height: '100%', overflow: 'hidden' }}
      ref={containerRef}
    >
      <KunSpin spinning={isLoading} style={{ height: '100%', position: 'absolute', top: '48px' }}>
        <TaskDAG
          taskDefinitions={taskDefinitions}
          selectedTaskDefIds={selectedTaskDefIds}
          setSelectedTaskDefIds={setSelectedTaskDefIds}
          viewportResetHookValue={viewportResetHookValue}
        />
        <DAGNodeInfoDrawer
          visible={drawerVisible}
          currentTaskDef={drawerTaskDef}
          getContainer={containerRef.current || false}
        />
        <div id="view-modification-tools" style={{ position: 'absolute', top: '55px', left: '8px' }}>
          {renderTools()}
        </div>
      </KunSpin>
    </div>
  );
});
