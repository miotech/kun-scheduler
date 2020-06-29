import React from 'react';
import { LinkProvidedProps } from '@vx/network/lib/types';

interface Link extends LinkProvidedProps<Link> {
  path: {
    x: number;
    y: number;
  }[];
}

export interface DAGTaskEdgeProps {
  link: Link;
}

export const DAGTaskEdge: React.FC<DAGTaskEdgeProps> = props => {
  const { path } = props.link;

  const d = (path || []).reduce((acc, point, i) => {
    return (i === 0) ?
      `M ${point.x},${point.y}` :
      `${acc} L ${point.x},${point.y}`
  }, '');

  return <path d={`${d}`} fill="none" stroke="#7A7E87" markerEnd="url(#arrowEnd)" />;
};

