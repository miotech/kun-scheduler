import React, { useMemo } from 'react';
import { Link } from 'umi';
import { Group } from '@visx/group';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { Badge, Popover, Tooltip } from 'antd';
import SafeUrlAssembler from 'safe-url-assembler';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { dayjs } from '@/utils/datetime-utils';
import useI18n from '@/hooks/useI18n';
import moment from 'moment-timezone';

export interface Node {
  x: number;
  y: number;
  height: number;
  width: number;
  title?: string;
  data?: TaskDefinition &
    Partial<{
      isCenter?: boolean;
    }> &
    Partial<{
      status: RunStatusEnum;
      startTime: string | number | Date;
      endTime: string | number | Date;
      taskDefinitionId: string;
      renderAsTaskRunDAG?: boolean;
    }>;
}

export interface DAGTaskNodeProps {
  node: Node;
}

function getLinkUrl(data: any) {
  if (data?.renderAsTaskRunDAG) {
    return SafeUrlAssembler()
      .template('/operation-center/scheduled-tasks/:id')
      .param({
        id: data.taskDefinitionId,
      })
      .query({
        taskRunId: data.id,
      })
      .toString();
  }
  if (data?.id) {
    return SafeUrlAssembler()
      .template('/data-development/task-definition/:id')
      .param({
        id: data.id,
      })
      .toString();
  }
  // else
  return '#';
}

function renderStatus(status: RunStatusEnum) {
  switch (status) {
    case 'RUNNING':
      return <Badge status="processing" text={status} />;
    case 'CREATED':
    case 'QUEUED':
    case 'UPSTREAM_FAILED':
      return <Badge status="warning" text={status} />;
    case 'ABORTED':
    case 'ABORTING':
    case 'FAILED':
      return <Badge status="error" text={status} />;
    case 'SUCCESS':
      return <Badge status="success" text={status} />;
    default:
      return <Badge status="default" text={status} />;
  }
}

export const DAGTaskNode: React.FC<DAGTaskNodeProps> = props => {
  // const ref = useRef() as RefObject<SVGGElement>;
  const { width, height, title, data } = props.node || {};

  const t = useI18n();

  const strokeColor = useMemo(() => {
    if (data?.isArchived) {
      return '#ebebeb';
    }
    if (data?.isDeployed) {
      return '#4cc5ca';
    }
    if (data?.status === 'RUNNING') {
      return '#4cc5ca';
    }
    if (data?.status === 'SUCCESS') {
      return '#62cf5c';
    }
    if (data?.status === 'ABORTED' || data?.status === 'FAILED') {
      return '#f16578';
    }
    // else
    return '#ffbe3d';
  }, [data]);

  const textColor = useMemo(() => {
    if (data?.isArchived) {
      return '#9c9c9c';
    }
    // else
    return '#262a2f';
  }, [data?.isArchived]);

  const rect = useMemo(() => {
    const content = data?.renderAsTaskRunDAG ? (
      <Popover
        title={title}
        overlayClassName="taskrun-dag-node-popover"
        content={
          <table style={{ tableLayout: 'fixed', minWidth: '320px' }}>
            <tbody>
              {data?.status ? (
                <tr>
                  <td>{t('scheduledTasks.property.stat')}</td>
                  <td>{renderStatus(data.status)}</td>
                </tr>
              ) : (
                <></>
              )}
              <tr>
                <td>{t('taskRun.property.startAt')}</td>
                <td>{data?.startTime ? dayjs(data?.startTime).format('YYYY-MM-DD HH:mm:ss') : '-'}</td>
              </tr>
              <tr>
                <td>{t('taskRun.property.endAt')}</td>
                <td>{data?.endTime ? dayjs(data?.endTime).format('YYYY-MM-DD HH:mm:ss') : '-'}</td>
              </tr>
              <tr>
                <td>{t('taskRun.property.duration')}</td>
                <td>
                  {data?.endTime && data?.startTime
                    ? moment.duration(moment(data.endTime).diff(moment(data.startTime))).format('h:mm:ss', {
                        trim: false,
                      })
                    : '-'}
                </td>
              </tr>
            </tbody>
          </table>
        }
      >
        <Link to={getLinkUrl(data)}>
          <p
            style={{
              fontSize: '12px',
              color: textColor,
              margin: 0,
              display: 'block',
              width,
              height,
              zIndex: 2,
              // pointerEvents: 'none',
              cursor: 'pointer',
              userSelect: 'none',
              textAlign: 'center',
            }}
          >
            <span style={{ color: data?.isCenter ? '#6997e3' : undefined }}>{title}</span>
          </p>
        </Link>
      </Popover>
    ) : (
      <Tooltip title={title}>
        <Link to={getLinkUrl(data)}>
          <p
            style={{
              fontSize: '12px',
              color: textColor,
              margin: 0,
              display: 'block',
              width,
              height,
              zIndex: 2,
              // pointerEvents: 'none',
              cursor: 'pointer',
              userSelect: 'none',
              textAlign: 'center',
            }}
          >
            <span style={{ color: data?.isCenter ? '#6997e3' : undefined }}>{title}</span>
          </p>
        </Link>
      </Tooltip>
    );

    return (
      <Group top={0} left={0}>
        <rect
          width={width}
          height={height}
          y={-height / 2}
          x={-width / 2}
          rx="8"
          stroke={strokeColor}
          strokeWidth={data?.isCenter ? 2 : 1}
          fill="#fff"
          cursor="pointer"
        />
        {/*
        <text
          dy=".33em"
          fontSize={9}
          fontFamily="Titillium Web, PingFangSC, Arial, sans-serif"
          textAnchor="middle"
          width={width}
          style={{
            pointerEvents: 'none',
            userSelect: 'none',
            overflowX: 'hidden',
            whiteSpace: 'nowrap',
          }}
          fill={textColor}
        >
          {title}
        </text>
        */}
        {/* TODO: foreign object is not supported by IE. Alternative approach is needed. */}
        <foreignObject x={-width / 2} y={-height / 2} width={width} height={height}>
          <div
            style={{
              background: 'transparent',
              overflow: 'visible',
            }}
          >
            {content}
          </div>
        </foreignObject>
      </Group>
    );
  }, [width, height, strokeColor, data?.isCenter, data?.id, title, textColor]);

  return rect;
};
