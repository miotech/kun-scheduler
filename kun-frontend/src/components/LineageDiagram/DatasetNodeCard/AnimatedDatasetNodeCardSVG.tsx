import React, { memo, useState } from 'react';
import { Animate } from 'react-move';
import isNil from 'lodash/isNil';
import { DatasetNodeCard, DatasetNodeCardProps } from '@/components/LineageDiagram/DatasetNodeCard/DatasetNodeCard';
import { easeExpOut } from 'd3';
import {
  NODE_DEFAULT_HEIGHT, NODE_DEFAULT_WIDTH, PORT_WIDTH
} from '@/components/LineageDiagram/LineageBoard/helpers/constants';

interface OwnProps {
  x: number;
  y: number;
  startX?: number;
  startY?: number;
  width?: number;
  height?: number;
}

type Props = OwnProps & DatasetNodeCardProps;

const EXIT_DURATION = 750;

export const AnimatedDatasetNodeCardSVG: React.FC<Props> = memo(function AnimatedDatasetNodeCard(props) {
  const { x, y, startX, startY, width, height, ...restProps } = props;
  const [ show, setShow ] = useState<boolean>(false);

  return (
    <Animate
      show={show}
      start={() => ({
        x: isNil(startX) ? startX : x,
        y: isNil(startY) ? startY : y,
        opacity: 1e-9,
      })}
      update={() => {
        return {
          x,
          y,
          timing: { duration: 750, ease: easeExpOut }
        };
      }}
      enter={() => {
        setShow(true);
      }}
      leave={() => {
        setTimeout(() => { setShow(false); }, EXIT_DURATION);
      }}
    >
      {(state) => {
        return (
          <g
            x={state.x}
            y={state.y}
            opacity={state.opacity}
          >
            <foreignObject
              width={(width || NODE_DEFAULT_WIDTH) + PORT_WIDTH}
              height={height || NODE_DEFAULT_HEIGHT}
            >
              <div style={{ position: 'relative', left: '10px' }}>
                <DatasetNodeCard
                  {...restProps}
                />
              </div>
            </foreignObject>

          </g>
        );
      }}
    </Animate>
  );
});
