import React, { FunctionComponent, useEffect, useMemo } from 'react';
import uniqueId from 'lodash/uniqueId';
import { useRequest } from 'ahooks';
import { DAGTaskGraph } from '@/components/DAGGraph';
import { KunSpin } from '@/components/KunSpin';
import { fetchDeployedTaskDAG } from '@/services/task-deployments/deployed-tasks';

import { DeployedTask } from '@/definitions/DeployedTask.type';
import { TaskNode, TaskRelation } from '@/components/DAGGraph/typings';

interface OwnProps {
  task: DeployedTask | null;
  width?: number;
  height?: number;
}

type Props = OwnProps;

const DeployedTaskDAG: FunctionComponent<Props> = (props) => {
  const { task, width, height } = props;

  const {
    data: dagData,
    loading,
    run: fetchDAGFromRemote,
  } = useRequest(fetchDeployedTaskDAG, {
    manual: true,
  });

  useEffect(() => {
    if (task) {
      fetchDAGFromRemote(task.id, {
        upstreamLevel: 3,
        downstreamLevel: 3,
      });
    }
  }, [
    fetchDAGFromRemote,
    task,
  ]);

  const DAGDom = useMemo(() => {
    if (loading) {
      return <KunSpin asBlock />;
    }
    if (dagData) {
      const nodes: TaskNode[] = dagData.nodes.map(task => {
        return {
          id: task.id,
          name: task.name,
        };
      });
      const edges: TaskRelation[] = dagData.edges.map(edge => ({
        id: uniqueId(),
        upstreamTaskId: edge.upstreamTaskId,
        downstreamTaskId: edge.downStreamTaskId,
      }));
      return (
        <DAGTaskGraph
          width={width}
          height={height}
          nodes={nodes || []}
          relations={edges || []}
        />
      );
    }
    // else
    return <></>;
  }, [
    loading,
    dagData,
    width,
    height,
  ]);

  return (
    <div data-tid="deployed-task-dag">
      {DAGDom}
    </div>
  );
};

export default DeployedTaskDAG;
