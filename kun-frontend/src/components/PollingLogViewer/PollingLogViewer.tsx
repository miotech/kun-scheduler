import React, { FC, useEffect } from 'react';
import c from 'classnames';
import { LazyLog, ScrollFollow } from 'react-lazylog';
import { useInterval } from 'ahooks';
import { KunSpin } from '@/components/KunSpin';

import { ServiceRespPromise } from '@/definitions/common-types';
import { TaskRunLog } from '@/definitions/TaskRun.type';

import './PollingLogViewer.less';

interface PollingLogViewerProps {
  pollInterval?: number;
  queryFn?: () => ServiceRespPromise<TaskRunLog>;
  startPolling?: boolean;
  className?: string;
}

const PollingLogViewer: FC<PollingLogViewerProps> = (props) => {
  const {
    pollInterval = 3000,
    queryFn = (() => Promise.resolve(null)),
    startPolling = false,
  } = props;

  // React Lazylog requires at least 1 line of text
  const [ text, setText ] = React.useState<string>('\n');
  const [ terminated, setTerminated ] = React.useState<boolean>(false);
  const [ loading, setLoading ] = React.useState<boolean>(false);

  /* When startPolling or query function changed, reset terminate flag */
  useEffect(() => {
    if (queryFn && startPolling) {
      setTerminated(false);
      setLoading(true);
    }
  }, [
    startPolling,
    queryFn,
  ]);

  useInterval(async () => {
    if (startPolling && !terminated) {
      const response = await queryFn();
      setLoading(false);
      if (response && response.logs) {
        setText(response.logs.join('\n') || '\n');
        setTerminated(response?.isTerminated || false);
      }
    }
  }, pollInterval);

  return (
    <div className={c('polling-lazylog-wrapper', props.className)}>
      <KunSpin
        spinning={loading}
        className="lazylog-spin-container"
      >
        <ScrollFollow
          startFollowing
          render={({ onScroll, follow, startFollowing, stopFollowing }) => (
            <LazyLog
              extraLines={0}
              enableSearch
              selectableLines
              text={text}
              // @ts-ignore
              onScroll={onScroll}
              follow={follow}
            />
          )}
        />
      </KunSpin>
    </div>
  );
};

export default PollingLogViewer;
