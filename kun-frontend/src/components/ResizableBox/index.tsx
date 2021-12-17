import { Resizable } from 'react-resizable'
import './index.css'
import React, { memo, useMemo, useState } from 'react';

interface OwnProps {
  width: number;
  height: number;
}

type Props = OwnProps;
export const ResizableBox: React.FC<Props> = memo(function ResizableBox(props) {
  const [width, setWidth] = useState(200);
  const onResize = (event, {size}) => {
    setWidth(size.width)
  };
  return (
    <Resizable axis='y' height={300} width={width} onResize={onResize}>
      <div className="box" style={{ width: width + 'px', height: 300 + 'px' }}>
        <span>Contents</span>
      </div>
    </Resizable>
  );
})