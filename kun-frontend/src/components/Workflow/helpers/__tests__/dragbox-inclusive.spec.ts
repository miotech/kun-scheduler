import { computeDragInclusive, DragIntersectionParams, NodeRectangle } from '../dragbox-inclusive';

describe('dragbox-inclusive', () => {
  it('should select inclusive node', () => {
    const drag: DragIntersectionParams = {
      dragStartX: 10,
      dragStartY: 10,
      dragEndX: 100,
      dragEndY: 100,
    };
    const node: NodeRectangle = {
      x: 20,
      y: 15,
      width: 70,
      height: 70,
    };
    const intersectResult = computeDragInclusive(drag, node);
    expect(intersectResult).toBeTruthy();
  });

  it('should select when box intersect node', () => {
    const drag: DragIntersectionParams = {
      dragStartX: 10,
      dragStartY: 10,
      dragEndX: 50,
      dragEndY: 50,
    };
    const node: NodeRectangle = {
      x: 20,
      y: 15,
      width: 70,   // 20 ~ 90
      height: 70,  // 15 ~ 85
    };
    const intersectResult = computeDragInclusive(drag, node);
    expect(intersectResult).toBeTruthy();
  });

  it('should not select when not inclusive or intersect', () => {
    const drag: DragIntersectionParams = {
      dragStartX: 10,
      dragStartY: 10,
      dragEndX: 10,
      dragEndY: 10,
    };
    const node: NodeRectangle = {
      x: 25,
      y: 30,
      width: 70,
      height: 70,
    };
    const intersectResult = computeDragInclusive(drag, node);
    expect(intersectResult).toBeFalsy();
  });
});
