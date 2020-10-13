import React, { memo } from 'react';
import { DropTargetMonitor, useDrop } from 'react-dnd';
import { connect } from 'react-redux';
import { useMount, useSize } from 'ahooks';
import { RootState } from '@/rematch/store';
import { TaskDefsDAG } from '@/pages/data-development/components/TaskDefsDAG';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';

import styles from './TasksGraphPanel.less';
import useRedux from '@/hooks/useRedux';
import SimpleErrorFallbackComponent from '@/components/SimpleErrorFallback';
import { ErrorBoundary } from 'react-error-boundary';

export interface TasksGraphPanelProps {
  taskDefinitionsForDAG: TaskDefinition[];
}

export const TasksGraphPanelComponent: React.FC<TasksGraphPanelProps> = memo(
function TasksGraphPanelComponent(props) {
  const [{ canDrop, isOver }, drop] = useDrop({
    accept: 'TaskTemplate',
    drop: (item: any) => ({
      ...item,
      droppedContainer: 'GraphPanel',
    }),
    collect: (monitor: DropTargetMonitor) => ({
      isOver: monitor.isOver(),
      canDrop: monitor.canDrop(),
    }),
  });

  const { dispatch } = useRedux(() => ({}));

  useMount(() => {
    dispatch.dataDevelopment.fetchTaskDefinitionsForDAG();
  });

  const dagPanelDom = document.getElementById('dag-panel');
  const size = useSize(dagPanelDom);

  const isActive = canDrop && isOver;

  return (
    <section ref={drop} id="dag-panel" className={styles.TasksGraphPanel}>
      {/* Drop masking layer */}
      {isActive ? <div className={styles.TaskGraphDropMask}>
        <div className={styles.MaskBg} />
        <p className={styles.MaskText}>Drop here to add</p>
      </div> : <></>}
      {/* DAG graph */}
      <ErrorBoundary FallbackComponent={SimpleErrorFallbackComponent}>
        <TaskDefsDAG
          width={size?.width || 1024}
          height={size?.height || 768}
          taskDefinitions={props.taskDefinitionsForDAG}
          // showMiniMap
        />
      </ErrorBoundary>
    </section>
  );
});

const mapStateToProps = (state: RootState) => ({
  taskDefinitionsForDAG: state.dataDevelopment.dagTaskDefs || [],
});

export const TasksGraphPanel = connect(mapStateToProps)(TasksGraphPanelComponent);
