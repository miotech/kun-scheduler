import React, { memo, useRef } from 'react';
import useRedux from '@/hooks/useRedux';
import { WorkflowCanvas } from '@/components/Workflow/canvas/Canvas.component';
import { useMount, useSize, useUnmount } from 'ahooks';

import LogUtils from '@/utils/logUtils';
import styles from './TaskDAG.module.less';

interface OwnProps {}

type Props = OwnProps;

export const logger = LogUtils.getLoggers('TaskDAG');

export const TaskDAG: React.FC<Props> = memo(function TaskDAG(props) {

  const {
    dispatch,
  } = useRedux<{}>(s => ({}));

  const containerRef = useRef<any>();
  const { width, height } = useSize(containerRef);

  return (
    <div
      ref={containerRef}
      className={styles.TaskDAGContainer}
      data-tid="task-dag-container"
    >
      <WorkflowCanvas
        width={(width || 3) - 2}
        height={(height || 3) - 2}
        nodes={[]}
        edges={[]}
      />
    </div>
  );
});
