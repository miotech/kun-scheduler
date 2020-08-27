import React, { useMemo } from 'react';
import { Link } from 'umi';
import { Group } from '@vx/group';
import {TaskDefinition} from "@/definitions/TaskDefinition.type";
import { Tooltip } from 'antd';
import SafeUrlAssembler from 'safe-url-assembler';

export interface Node {
  x: number;
  y: number;
  height: number;
  width: number;
  title?: string;
  data?: TaskDefinition;
}

export interface DAGTaskNodeProps {
  node: Node;
}

export const DAGTaskNode: React.FC<DAGTaskNodeProps> = props => {
  // const ref = useRef() as RefObject<SVGGElement>;
  const { x, y, width, height, title, data } = props.node || {};

  const strokeColor = useMemo(() => {
    if (data?.isArchived) {
      return '#ebebeb';
    }
    if (data?.isDeployed) {
      return '#4cc5ca';
    }
    // else
    return '#ffbe3d';
  }, [
    data?.isArchived,
    data?.isDeployed,
  ]);

  const textColor = useMemo(() => {
    if (data?.isArchived) {
      return '#9c9c9c';
    }
    // else
    return '#262a2f';
  }, [
    data?.isArchived
  ]);

  const rect = useMemo(() => {
    return (
      <Group
        top={0}
        left={0}
      >
        <rect
          width={width}
          height={height}
          y={-height / 2}
          x={-width / 2}
          rx="8"
          stroke={strokeColor}
          strokeWidth={1}
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
        <foreignObject
          x={-width / 2}
          y={-height / 2}
          width={width}
          height={height}
        >
          <div
            style={{
              background: 'transparent',
              overflow: 'visible',
            }}
          >
            <Tooltip title={title}>
              <Link to={data?.id ? SafeUrlAssembler().template('/data-development/task-definition/:id').param({
                id: data.id,
              }).toString() : '#'}>
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
                  {title}
                </p>
              </Link>
            </Tooltip>
          </div>
        </foreignObject>
      </Group>
    )
  }, [
    width,
    height,
    x,
    y,
    title,
  ]);

  return rect;
};
