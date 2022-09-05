import React, { memo, useMemo, useCallback, useState } from 'react';
import { Link } from 'umi';
import { Group } from '@visx/group';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { Badge, Popover, Tooltip } from 'antd';
import SafeUrlAssembler from 'safe-url-assembler';
import { RunStatusEnum } from '@/definitions/StatEnums.type';
import { dayjs } from '@/utils/datetime-utils';
import useI18n from '@/hooks/useI18n';
import moment from 'moment-timezone';
import { taskColorConfig } from '@/constants/colorConfig';
import { CardPort } from '@/components/LineageDiagram/DatasetNodeCard/CardPort';
import c from 'clsx';
import { DagState } from '@/rematch/models/dag';
import useRedux from '@/hooks/useRedux';
import TextContainer from '../TextContainer/TextContainer';
import styles from './DAGTaskNode.less';

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
      failedUpstreamTaskRuns: {
        id: string;
        name: string;
      }[];
    }>;
}

export interface Props {
  expandUpstreamDAG: (id: string) => Promise<void>;
  expandDownstreamDAG: (id: string) => Promise<void>;
  closeUpstreamDag: (id: string) => void;
  closeDownstreamDag: (id: string) => void;
  node: Node | null;
}

function getLinkUrl(data: any) {
  if (data?.renderAsTaskRunDAG) {
    return SafeUrlAssembler()
      .template('/operation-center/task-run-id/:taskRunId')
      .param({
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
  const color: string = taskColorConfig[status] ? taskColorConfig[status] : taskColorConfig.DEFAULT;
  return <Badge color={color} text={status} />;
}

export const DAGTaskNode = memo((props: Props) => {
  // const ref = useRef() as RefObject<SVGGElement>;
  const { expandUpstreamDAG, expandDownstreamDAG, closeUpstreamDag, closeDownstreamDag, node } = props;
  const { width, height, title, data } = node || {};
  const [upstreamLoading, setUpstreamLoading] = useState(false);
  const [downstreamLoading, setDownstreamLoading] = useState(false);
  const t = useI18n();
  const { selector, dispatch } = useRedux<DagState>(state => state.dag);
  const { expandUpstreamNodeIds, expandDownstreamNodeIds, currentClickId } = selector;

  const strokeColor = useMemo(() => {
    if (data?.isArchived) {
      return '#ebebeb';
    }
    if (data?.isDeployed) {
      return '#4cc5ca';
    }
    if (data?.status && taskColorConfig[data?.status]) {
      return taskColorConfig[data?.status];
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

  const expandUpstreamTaskRunDAG = useCallback(async () => {
    setUpstreamLoading(true);
    await expandUpstreamDAG(data?.id);
    dispatch.dag.updateState({
      currentClickId: data?.id,
    });
    // setUpstreamLoading(false); dag组件会被销毁，所以不能再设置
  }, [data, dispatch, expandUpstreamDAG]);

  const expandDownstreamTaskRunDAG = useCallback(async () => {
    setDownstreamLoading(true);
    await expandDownstreamDAG(data?.id);
    dispatch.dag.updateState({
      currentClickId: data?.id,
    });
    // setDownstreamLoading(false);
  }, [data, dispatch, expandDownstreamDAG]);

  const closeUpstreamNode = useCallback(() => {
    closeUpstreamDag(data?.id);
    dispatch.dag.updateState({
      currentClickId: data?.id,
    });
  }, [data, dispatch, closeUpstreamDag]);

  const closeDownstreamNode = useCallback(() => {
    closeDownstreamDag(data?.id);
    dispatch.dag.updateState({
      currentClickId: data?.id,
    });
  }, [data, dispatch, closeDownstreamDag]);

  const upstreamPortState = useMemo(() => {
    if (upstreamLoading) {
      return 'loading';
    }
    return expandUpstreamNodeIds.includes(data.id) ? 'expanded' : 'collapsed';
  }, [upstreamLoading, expandUpstreamNodeIds, data?.id]);

  const downstreamPortState = useMemo(() => {
    if (downstreamLoading) {
      return 'loading';
    }
    return expandDownstreamNodeIds.includes(data.id) ? 'expanded' : 'collapsed';
  }, [downstreamLoading, expandDownstreamNodeIds, data?.id]);

  const rect = useMemo(() => {
    const content = data?.renderAsTaskRunDAG ? (
      <Popover
        title={
          <TextContainer maxWidth={250} ellipsis tooltipTitle={title}>
            {title}
          </TextContainer>
        }
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
              {!!data.failedUpstreamTaskRuns && (
                <tr>
                  <td>{t('taskRun.property.failedLink')}</td>
                  <td>
                    {data.failedUpstreamTaskRuns.map(i => (
                      <TextContainer key={i.id} maxWidth={190} ellipsis tooltipTitle={i.name} mouseEnterDelay={0.5}>
                        <Link to={`/operation-center/task-run-id/${i.id}`} target="_blank">
                          {i.name}
                        </Link>
                      </TextContainer>
                    ))}
                  </td>
                </tr>
              )}
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
              width: width - 4,
              height: height - 20,
              zIndex: 2,
              // pointerEvents: 'none',
              cursor: 'pointer',
              userSelect: 'none',
              textAlign: 'center',
            }}
          >
            <span className={styles.nodeText} style={{ color: data?.isCenter ? '#6997e3' : undefined }}>
              {title}
            </span>
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
              width: width - 4,
              height: height - 20,
              zIndex: 2,
              // pointerEvents: 'none',
              cursor: 'pointer',
              userSelect: 'none',
              textAlign: 'center',
            }}
          >
            <span className={styles.nodeText} style={{ color: data?.isCenter ? '#6997e3' : undefined }}>
              {title}
            </span>
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
          // rx="8"
          // stroke={strokeColor}
          // strokeWidth={data?.isCenter ? 2 : 1}
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
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
          >
            {currentClickId === data?.id && (
              <div style={{ border: `2px solid #1a73e8`, position: 'relative', top: '10px' }}> {content} </div>
            )}
            {currentClickId !== data?.id && (
              <div style={{ border: `1px solid ${strokeColor}`, position: 'relative', top: '10px' }}> {content} </div>
            )}

            <CardPort
              className={c(styles.button, styles.buttonTop)}
              portState={upstreamPortState}
              onExpand={expandUpstreamTaskRunDAG}
              onCollapse={closeUpstreamNode}
            />
            <CardPort
              className={c(styles.button, styles.buttonBottom)}
              portState={downstreamPortState}
              onExpand={expandDownstreamTaskRunDAG}
              onCollapse={closeDownstreamNode}
            />
          </div>
        </foreignObject>
      </Group>
    );
  }, [
    width,
    height,
    strokeColor,
    data,
    title,
    textColor,
    expandUpstreamTaskRunDAG,
    expandDownstreamTaskRunDAG,
    closeUpstreamNode,
    closeDownstreamNode,
    currentClickId,
    downstreamPortState,
    upstreamPortState,
    t,
  ]);

  return rect;
});
