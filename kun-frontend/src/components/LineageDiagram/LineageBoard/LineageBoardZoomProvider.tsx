import React, { memo, useCallback, useState } from 'react';
import { Zoom } from '@visx/zoom';
import { LineageBoard } from '@/components/LineageDiagram/LineageBoard/LineageBoard';
import LogUtils from '@/utils/logUtils';

interface OwnProps {
  width: number;
  height: number;
  scaleXMin?: number;
  scaleXMax?: number;
  scaleYMin?: number;
  scaleYMax?: number;
  initialTransform?: Transform;
  children?: React.ReactNode;
}

type Props = OwnProps;

interface Transform {
  scaleX: number;
  scaleY: number;
  translateX: number;
  translateY: number;
  skewX: number;
  skewY: number;
}

export const logger = LogUtils.getLoggers('LineageBoardZoomProvider');

export const LineageBoardZoomProvider: React.FC<Props> = memo(function LineageBoardZoomProvider(props) {
  const {
    width,
    height,
    scaleXMin,
    scaleXMax,
    scaleYMin,
    scaleYMax,
    initialTransform,
    children,
  } = props;

  const [ isDragging, setIsDragging ] = useState<boolean>(false);

  const handleDragStart = useCallback((zoom) => (ev: any) => {
    zoom.dragStart(ev);
    setIsDragging(true);
  }, []);

  const handleDragMove = useCallback((zoom) => (ev: any) => {
    zoom.dragMove(ev);
  }, []);

  const handleDragEnd = useCallback((zoom) => (ev: any) => {
    zoom.dragEnd(ev);
    setIsDragging(false);
  }, []);

  return (
    <Zoom
      width={width}
      height={height}
      scaleXMin={scaleXMin}
      scaleXMax={scaleXMax}
      scaleYMin={scaleYMin}
      scaleYMax={scaleYMax}
      transformMatrix={initialTransform}
    >
      {zoom => {
        return React.Children.map(children, child => {
          if (React.isValidElement(child) && child.type === LineageBoard) {
            return React.cloneElement(child, {
              zoom,
              onDragStart: handleDragStart(zoom),
              onDragMove: handleDragMove(zoom),
              onDragEnd: handleDragEnd(zoom),
              backgroundCursor: isDragging ? 'grabbing' : 'grab',
            });
          }
          // else
          return child;
        });
      }}
    </Zoom>
  );
});
