import React, { memo } from 'react';
import { FailedUpstreamTaskRun } from '@/definitions/TaskRun.type';
import TextContainer from '@/components/TextContainer/TextContainer';
import { Link } from 'umi';

interface Props {
  failedUpstreamTaskRuns: FailedUpstreamTaskRun[] | undefined;
}

export default memo(function UpstreamFailed(props: Props) {
  const { failedUpstreamTaskRuns } = props;
  return (
    <div>
      {failedUpstreamTaskRuns?.map(i => (
        <TextContainer key={i.id} maxWidth={190} ellipsis tooltipTitle={i.task.name} mouseEnterDelay={0.5}>
          <Link to={`/operation-center/task-run-id/${i.id}`} target="_blank">
            {i.task.name}
          </Link>
        </TextContainer>
      ))}
    </div>
  );
});
