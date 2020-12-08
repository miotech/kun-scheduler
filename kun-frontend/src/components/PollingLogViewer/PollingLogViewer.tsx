import React, { FC, useCallback, useEffect } from 'react';
import c from 'clsx';
import { LazyLog, ScrollFollow } from 'react-lazylog';
import { Button, Tooltip } from 'antd';
import { CopyOutlined, DownloadOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';
import { useInterval } from 'ahooks';
import dayjs from 'dayjs';
import { KunSpin } from '@/components/KunSpin';

import { writeText } from 'clipboard-polyfill/text';
import FileSaver from 'file-saver';

import { ServiceRespPromise } from '@/definitions/common-types';
import { TaskRunLog } from '@/definitions/TaskRun.type';

import './PollingLogViewer.less';

interface PollingLogViewerProps {
  pollInterval?: number;
  queryFn?: () => ServiceRespPromise<TaskRunLog>;
  startPolling?: boolean;
  className?: string;
  saveFileName?: string;
}

const PollingLogViewer: FC<PollingLogViewerProps> = (props) => {
  const {
    pollInterval = 3000,
    queryFn = (() => Promise.resolve(null)),
    startPolling = false,
    saveFileName,
  } = props;

  const t = useI18n();

  // React Lazylog requires at least 1 line of text
  const [ text, setText ] = React.useState<string>('\n');
  const [ terminated, setTerminated ] = React.useState<boolean>(false);
  const [ loading, setLoading ] = React.useState<boolean>(false);
  const [ copied, setCopied ] = React.useState<boolean>(false);

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

  const handleCopyToClipboard = useCallback(function handleCopyToClipboard() {
    writeText(text).then(() => {
      setCopied(true);
    });
  }, [
    text,
  ]);

  const handleDownloadLog = useCallback(function handleDownloadLog() {
    const blob = new Blob([text], {
      type: 'text/plain;charset=utf-8'
    });
    FileSaver.saveAs(blob, saveFileName ?
      `${saveFileName}-${dayjs().format('YYYY-MM-DD_HH_mm_ss')}.log` :
      `${dayjs().format('YYYY-MM-DD_HH_mm_ss')}.log`);
  }, [
    text,
    saveFileName,
  ]);

  return (
    <div className={c('polling-lazylog-wrapper', props.className)}>
      <KunSpin
        spinning={loading}
        className="lazylog-spin-container"
      >
        <div className="lazylog-button-group">
          <Tooltip
            title={copied ? t('common.reactlazylog.copyToClipboardSuccess') : t('common.reactlazylog.copyToClipboard')}
            onVisibleChange={() => { setCopied(false); }}
          >
            <Button
              className="tool-btn"
              size="small"
              type="link"
              icon={<CopyOutlined />}
              onClick={handleCopyToClipboard}
            />
          </Tooltip>
          <Tooltip
            title={t('common.reactlazylog.downloadAsFile')}
            onVisibleChange={() => { setCopied(false); }}
          >
            <Button
              className="tool-btn"
              size="small"
              type="link"
              icon={<DownloadOutlined />}
              onClick={handleDownloadLog}
            />
          </Tooltip>
        </div>
        <ScrollFollow
          startFollowing
          render={({ onScroll, follow}) => (
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
