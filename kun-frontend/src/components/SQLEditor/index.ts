import { dynamic } from 'umi';
import { SQLEditorProps } from '@/components/SQLEditor/SQLEditor';

// Dynamically load SQL Editor
// @ts-ignore
export const SQLEditor = dynamic({
  async loader() {
    const { SQLEditor: SQLEditorComponent } = await import('./SQLEditor');
    return SQLEditorComponent;
  },
}) as React.FC<SQLEditorProps>;
