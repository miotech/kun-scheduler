/**
 * Use this wrapper to avoid error of "Cannot have two HTML5 backends at the same time"
 * Reference: https://github.com/react-dnd/react-dnd/issues/186
 */

import React, { useRef } from 'react';
import { DragDropManager } from 'dnd-core';
import { createDndContext, DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';

const WithDndContext: React.FC<any> = ({ children }) => {
  const manager = useRef(createDndContext(HTML5Backend));

  return (
    <DndProvider
      manager={manager.current.dragDropManager as DragDropManager}
    >
      {children}
    </DndProvider>
  );
};

export default WithDndContext;
