import React, { memo, useCallback } from 'react';
import { Drag as VisxDrag } from '@visx/drag';
import { Transform } from '@/components/Workflow/Workflow.typings';

type Point = {
  x: number;
  y: number;
};

type DragState = Partial<Point> & {
  dx: number;
  dy: number;
  isDragging: boolean;
};

interface OwnProps {
  width?: number;
  height?: number;
  onDragStart?: (dragState: DragState, canvasTransform: Transform) => any;
  onDragMove?: (dragState: DragState, canvasTransform: Transform) => any;
  onDragEnd?: (dragState: DragState, canvasTransform: Transform) => any;
  transform?: Transform;
}

type Props = OwnProps;

export const Drag: React.FC<Props> = memo(function Drag(props) {
  const {
    width = 0,
    height = 0,
    onDragStart,
    onDragMove,
    onDragEnd,
    transform = {
      scaleX: 1,
      scaleY: 1,
      transformX: 0,
      transformY: 0,
    },
  } = props;

  const handleDragStart = useCallback(function handleDragStart(dragState: DragState) {
    if (onDragStart) {
      onDragStart(dragState, transform);
    }
  }, [
    transform,
    onDragStart,
  ]);

  const handleDragMove = useCallback(function handleDragMove({ x = 0, y = 0, dx = 0, dy = 0, isDragging = false }: Partial<DragState>) {
    if (onDragMove) {
      onDragMove({ x, y, dx, dy, isDragging }, transform);
    }
  }, [
    transform,
    onDragMove,
  ]);

  const handleDragEnd = useCallback(function handleDragEnd(dragState: DragState) {
    if (onDragEnd) {
      onDragEnd(dragState, transform);
    }
  }, [
    transform,
    onDragEnd,
  ]);

  const dragInternalRenderer = useCallback(function dragInternalRenderer(dragState) {
    const {
      x = 0,
      y = 0,
      dx,
      dy,
      isDragging,
      dragStart,
      dragEnd,
      dragMove,
    } = dragState;

    return (
      <svg width={width} height={height}>
        {/* drag selection rectangle box */}
        {isDragging ? (
          <rect
            fill="rgba(221, 221, 221, 0.25)"
            stroke="#444"
            strokeWidth={1}
            strokeDasharray="5,5"
            x={Math.min(x, x + dx)}
            y={Math.min(y, y + dy)}
            width={Math.abs(dx)}
            height={Math.abs(dy)}
          />
        ) : null}
        {/* Dragging area */}
        <rect
          fill="transparent"
          width={width}
          height={height}
          onMouseDown={dragStart}
          onMouseUp={dragEnd}
          onMouseMove={dragMove}
          onTouchStart={dragStart}
          onTouchEnd={dragEnd}
          onTouchMove={dragMove}
          cursor={isDragging ? 'crosshair' : 'default'}
        />
      </svg>
    );

  }, [
    height,
    width,
  ]);

  return (
    <div style={{ position: 'absolute', top: 0, left: 0, width, height }}>
      {/* drag */}
      <VisxDrag
        width={width}
        height={height}
        resetOnStart
        onDragStart={handleDragStart}
        onDragMove={handleDragMove}
        onDragEnd={handleDragEnd}
      >
        {dragInternalRenderer}
      </VisxDrag>
    </div>
  );
});
