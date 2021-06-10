import React from 'react';
// @ts-ignore
import { colDropTarget, colDragSource } from './drag-drop';

interface OwnProps {
  className?: string;
  columns?: { label: string; width: string | number }[];
  onColumnDrop?: any;
}

type Props = OwnProps;

const Header = colDropTarget(colDragSource((props: any) => {
  const { col, connectDragSource, connectDropTarget, isOver } = props;
  const className = isOver ? 'cell read-only drop-target' : 'cell read-only';
  return connectDropTarget(
    connectDragSource(
      <th className={className} style={{ width: col.width }}>{col.label}</th>
    ));
}));


export class SheetRenderer extends React.PureComponent<Props> {
  render () {
    const { className, columns, onColumnDrop } = this.props;
    return (
      <table className={className}>
        <thead>
          <tr>
            {/* eslint-disable-next-line jsx-a11y/control-has-associated-label */}
            {/* <th className='cell read-only row-handle' key='$$actionCell' /> */}
            {
              (columns || []).map((col, index) => (
                <Header key={col.label} col={col} columnIndex={index} onColumnDrop={onColumnDrop} />
              ))
            }
          </tr>
        </thead>
        <tbody>
          {this.props.children}
        </tbody>
      </table>
    );
  }
}
