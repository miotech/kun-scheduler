import { Intersection } from 'kld-intersections';
import { Point2D } from 'kld-affine';

export interface DragIntersectionParams {
  dragStartX: number;
  dragStartY: number;
  dragEndX: number;
  dragEndY: number;
}

export interface NodeRectangle {
  x: number;
  y: number;
  width: number;
  height: number;
}

export function computeDragIntersection(drag: DragIntersectionParams, nodeInfo: NodeRectangle): boolean {
  return Intersection.intersectRectangleRectangle(
    new Point2D(drag.dragStartX, drag.dragStartY),
    new Point2D(drag.dragEndX, drag.dragEndY),
    new Point2D(nodeInfo.x, nodeInfo.y),
    new Point2D(nodeInfo.x + nodeInfo.width, nodeInfo.y + nodeInfo.height)
  ).points.length > 0;
}
