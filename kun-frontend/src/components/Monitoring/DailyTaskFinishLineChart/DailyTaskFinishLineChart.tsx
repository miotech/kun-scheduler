import React, { memo, useCallback, useMemo } from 'react';
import { Group } from '@vx/group';
import { scaleTime, scaleLinear } from '@vx/scale';
import { AxisLeft, AxisBottom } from '@vx/axis';
import { curveLinear } from '@vx/curve';
import { localPoint } from '@vx/event';
import { Line, LinePath } from '@vx/shape';
import { useTooltip, useTooltipInPortal } from '@vx/tooltip';
import LogUtils from '@/utils/logUtils';

import { bisectCenter } from './helpers/bisect';

import './DailyTaskFinishLineChart.global.less';

export interface DailyTaskFinishCount {
  /** x-axis, date */
  time: Date | string | number;
  /** y-axis, count */
  taskCount: number;
}

const dateNormalize = (d: Date | string | number): number => {
  return new Date(d).valueOf();
};

const HORIZONTAL_OFFSET = 65;
const VERTICAL_OFFSET = 25;

export interface DailyTaskFinishLineChartProps {
  /** line chart component width in px */
  width?: number;
  /** line chart component height in px */
  height?: number;
  /** rendering data */
  data: DailyTaskFinishCount[];
}

// accessors
const toX = (d: DailyTaskFinishCount) => new Date(d.time).valueOf();
const toY = (d: DailyTaskFinishCount) => d.taskCount;

const logger = LogUtils.getLoggers('DailyTaskFinishLineChart');

export const DailyTaskFinishLineChart: React.FC<DailyTaskFinishLineChartProps> = memo(props => {
  const {
    width = 1024,
    height = 768,
    data = [],
  } = props;

  const {
    tooltipData,
    tooltipLeft,
    tooltipTop,
    tooltipOpen,
    showTooltip,
    hideTooltip,
  } = useTooltip<DailyTaskFinishCount & { x: number, y: number }>();

  // bounds
  const xMax = width - 120;
  const yMax = height - 80;

  const xScale = useMemo(() => scaleTime({
    range: [0, xMax],
    round: true,
    domain: [
      Math.min(...data.map(toX)),
      Math.max(...data.map(toX)),
    ],
  }), [
    data,
    xMax,
  ]);

  const yScale = useMemo(() => scaleLinear({
    range: [0, yMax],
    round: true,
    domain: [
      Math.max(...data.map(toY)),
      0,
    ],
  }), [
    data,
    yMax,
  ]);

  const xNumTicks = useMemo(() => Math.min(10, data.length), [
    data.length,
  ]);

  const { containerRef, TooltipInPortal } = useTooltipInPortal({
    // use TooltipWithBounds
    detectBounds: true,
    // when tooltip containers are scrolled, this will correctly update the Tooltip position
    scroll: true,
  });

  const normalizedTimestamps = useMemo(() => data.map(d => dateNormalize(d.time)), [data]);

  const handleMoveEvent = useCallback((event: React.MouseEvent<SVGRectElement> | React.TouchEvent<SVGRectElement>) => {
    const coords = localPoint(event);
    logger.trace('coords = %o;', coords);
    // What's current coordinate of user's mouse?
    const { x = 0, y = 0 } = coords || {};
    // get projected x value on x-axis
    const x0 = xScale.invert(x - HORIZONTAL_OFFSET || 0);
    // find the nearest left record by bisect algorithm
    const nearestIdx = bisectCenter(normalizedTimestamps, x0.valueOf());
    const datum = data[nearestIdx === data.length ? nearestIdx - 1 : nearestIdx];
    showTooltip({
      tooltipLeft: x,
      tooltipTop: y,
      tooltipData: {
        ...datum,
        x: xScale(dateNormalize(datum.time)),
        y: yScale(datum.taskCount),
      },
    });
  }, [
    xScale,
    yScale,
    normalizedTimestamps,
    data,
    showTooltip,
  ]);

  const renderTooltip = useCallback((tooltipVisible: boolean) => {
    if (tooltipVisible) {
      return (
        <TooltipInPortal
          key={Math.random()}
          top={tooltipTop}
          left={tooltipLeft}
        >
          <div>
            <div>Count: {tooltipData?.taskCount}</div>
            <div>Time: {new Date(tooltipData?.time || 0).toISOString()}</div>
          </div>
        </TooltipInPortal>
      );
    }
    // else
    return <></>;
  }, [
    tooltipTop,
    tooltipLeft,
    tooltipData?.taskCount,
    tooltipData?.time,
  ]);

  const verticalLine = useMemo(() => {
    if (tooltipData) {
      return (
        <g>
          <Line
            from={{ x: tooltipData.x, y: 0 }}
            to={{ x: tooltipData.x, y: yMax }}
            stroke="#75daad"
            strokeWidth={2}
            pointerEvents="none"
            strokeDasharray="5,2"
          />
          <circle
            cx={tooltipData.x}
            cy={tooltipData.y}
            r={4}
            fill="#75daad"
            stroke="white"
            strokeWidth={2}
            pointerEvents="none"
          />
        </g>
      );
    }
    return <></>;
  }, [tooltipData, yMax]);

  return (
    <div className="daily-task-finish-line-chart">
      <svg ref={containerRef} width={width} height={height}>
        <Group
          top={VERTICAL_OFFSET}
          left={HORIZONTAL_OFFSET}
        >
          <AxisLeft
            scale={yScale}
            numTicks={10}
            label="Count"
          />
          <AxisBottom
            orientation="bottom"
            scale={xScale}
            label="Day"
            labelOffset={15}
            numTicks={xNumTicks}
            top={yMax}
          />
          {/* line path of diagram */}
          <LinePath
            data={data}
            curve={curveLinear}
            x={d => xScale(toX(d))}
            y={d => yScale(toY(d))}
            cursor="pointer"
            stroke="#cfb162"
            strokeWidth={1.5}
          />
          {verticalLine}
          {/* event provider mask */}
          <rect
            width={xMax}
            height={yMax}
            fill="transparent"
            // onMouseOver={handleMouseOver}
            onMouseMove={handleMoveEvent}
            onTouchMove={handleMoveEvent}
            onMouseOut={hideTooltip}
          />
        </Group>
      </svg>
      {renderTooltip(tooltipOpen)}
    </div>
  );
});
