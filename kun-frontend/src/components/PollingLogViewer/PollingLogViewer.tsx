import React, { FC, useCallback, useEffect } from 'react';
import c from 'clsx';
import { LazyLog, ScrollFollow } from 'react-lazylog';
import { Button, message, Tooltip } from 'antd';
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
import LogUtils from '@/utils/logUtils';

interface PollingLogViewerProps {
  pollInterval?: number;
  queryFn?: () => ServiceRespPromise<TaskRunLog>;
  startPolling?: boolean;
  className?: string;
  saveFileName?: string;
  presetLineLimit?: number;
  onDownload?: () => Promise<string[]>;
}

const logger = LogUtils.getLoggers('PollingLogViewer');

const PollingLogViewer: FC<PollingLogViewerProps> = function PollingLogViewer(props) {
  const {
    pollInterval = 3000,
    queryFn = () => Promise.resolve(null),
    startPolling = false,
    saveFileName,
    presetLineLimit,
    onDownload,
  } = props;

  const t = useI18n();

  // React Lazylog requires at least 1 line of text
  const [text, setText] = React.useState<string>('\n');
  const [lines, setLines] = React.useState<number>(0);
  const [fullLogDownloading, setFullLogDownloading] = React.useState<boolean>(false);
  const [terminated, setTerminated] = React.useState<boolean>(false);
  const [loading, setLoading] = React.useState<boolean>(false);
  const [logNotFound, setLogNotFound] = React.useState<boolean>(false);
  const [copied, setCopied] = React.useState<boolean>(false);
  const [lastRequestReturned, setLastRequestReturned] = React.useState<boolean>(true);

  /* When startPolling or query function changed, reset terminate flag */
  useEffect(() => {
    if ((queryFn != null) && startPolling) {
      setTerminated(false);
      setLastRequestReturned(true);
      setLoading(true);
    }
  }, [startPolling, queryFn]);

  useInterval(async () => {
    if (startPolling && lastRequestReturned && !terminated) {
      setLastRequestReturned(false);
      const response = await queryFn();
      setLastRequestReturned(true);
      setLoading(false);
      if (response && response.logs == null) {
        setLogNotFound(true);
        setLines(0);
      } else if (response && response.logs) {
        setText(response.logs.join('\n') || '\n');
        setLines(response.logs.length);
        setLogNotFound(false);
        setTerminated(response?.isTerminated || false);
      }
    }
  }, pollInterval);

  const handleCopyToClipboard = useCallback(
    function handleCopyToClipboard() {
      writeText(text).then(() => {
        setCopied(true);
      });
    },
    [text],
  );

  const handleDownloadLogDefault = useCallback(
    function handleDownloadLog() {
      const blob = new Blob([text], {
        type: 'text/plain;charset=utf-8',
      });
      FileSaver.saveAs(
        blob,
        saveFileName
          ? `${saveFileName}-${dayjs().format('YYYY-MM-DD_HH_mm_ss')}.log`
          : `${dayjs().format('YYYY-MM-DD_HH_mm_ss')}.log`,
      );
    },
    [text, saveFileName],
  );

  const handleDownloadLog = onDownload
    ? async function downloadLogWrapperFn() {
        const dismiss = message.loading('Downloading...', 0);
        setFullLogDownloading(true);
        try {
          const fullLogText = await onDownload();
          const blob = new Blob([fullLogText.join('\n')], {
            type: 'text/plain;charset=utf-8',
          });
          FileSaver.saveAs(
            blob,
            saveFileName
              ? `${saveFileName}-${dayjs().format('YYYY-MM-DD_HH_mm_ss')}.log`
              : `${dayjs().format('YYYY-MM-DD_HH_mm_ss')}.log`,
          );
        } catch (e) {
          logger.error('Failed to download full log content');
          logger.error('Error =', e);
          message.error('Failed to fetch full log content.');
        } finally {
          dismiss();
          setFullLogDownloading(false);
        }
      }
    : handleDownloadLogDefault;

  return (
    <div className={c('polling-lazylog-wrapper', props.className)}>
      <KunSpin
        spinning={loading || logNotFound}
        tip={logNotFound ? t('common.reactlazylog.logNotFoundTip') : undefined}
        className="lazylog-spin-container"
      >
        {presetLineLimit != null && lines >= presetLineLimit ? (
          <div className="lazylog-line-limit-hint">
            {t('common.reactlazylog.showPartLinesOnly', { lines: presetLineLimit })}
          </div>
        ) : (
          <></>
        )}
        <div className="lazylog-button-group">
          <Tooltip
            title={copied ? t('common.reactlazylog.copyToClipboardSuccess') : t('common.reactlazylog.copyToClipboard')}
            onVisibleChange={() => {
              setCopied(false);
            }}
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
            onVisibleChange={() => {
              setCopied(false);
            }}
          >
            <Button
              className="tool-btn"
              size="small"
              type="link"
              icon={<DownloadOutlined />}
              onClick={handleDownloadLog as any}
              disabled={fullLogDownloading}
            />
          </Tooltip>
        </div>
        <ScrollFollow
          startFollowing
          render={({ onScroll, follow }) => (
            <LazyLog
              extraLines={1}
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
