import React from 'react';
import { EventEmitter } from 'ahooks/lib/useEventEmitter';

export interface TaskDevelopmentViewContext {
  viewportCenter$: EventEmitter<string | number> | null;
}

export const ViewContext = React.createContext<TaskDevelopmentViewContext>({
  viewportCenter$: null,
});
