import React from 'react';

interface OwnProps {
  className?: string;
  columns?: { label: string; width: string | number }[];
}

type Props = OwnProps;


export class SheetRenderer extends React.PureComponent<Props> {
  render () {
    const { className, columns } = this.props;
    return (
      <table className={className}>
        <thead>
          <tr>
            {/* eslint-disable-next-line jsx-a11y/control-has-associated-label */}
            {/* <th className='cell read-only row-handle' key='$$actionCell' /> */}
            {
              (columns || []).map((col, index) => (
                // eslint-disable-next-line react/no-array-index-key
                <th className="cell read-only" style={{ width: col.width }} key={`${col.label}-${index}`} >
                  {col.label}
                </th>
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
