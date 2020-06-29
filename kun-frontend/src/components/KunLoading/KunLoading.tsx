import React, { memo } from 'react';

export const KunLoading: React.FC<{}> = memo(() => {
  return (
    <svg viewBox="0 0 175 75">
      <g>
        <path
          className="kun-alphabet-path"
          fill="white"
          stroke="black"
          strokeWidth="4"
          d="M 46.68 71.094 L 14.014 33.691 L 2.637 44.189 L 2.637 71.094 L 0 71.094 L 0 0 L 2.637 0 L 2.637 41.113 L 14.355 29.639 L 43.848 0 L 47.656 0 L 15.967 31.885 L 50.098 71.094 L 46.68 71.094 Z"
          vectorEffect="non-scaling-stroke"
        />
      </g>
      <g transform="matrix(1,0,0,1,60,0)">
        <path
          className="kun-alphabet-path"
          fill="white"
          stroke="black"
          strokeWidth="4"
          d="M 45.947 0 L 48.584 0 L 48.584 48.145 Q 48.584 59.424 42.041 65.747 A 21.834 21.834 0 0 1 31.456 71.323 A 32.264 32.264 0 0 1 24.316 72.07 A 31.987 31.987 0 0 1 16.879 71.254 A 21.754 21.754 0 0 1 6.592 65.747 A 20.986 20.986 0 0 1 0.681 54.877 A 31.092 31.092 0 0 1 0 48.34 L 0 0 L 2.637 0 L 2.637 48.047 A 26.478 26.478 0 0 0 3.437 54.737 A 18.802 18.802 0 0 0 8.569 63.672 Q 14.502 69.434 24.316 69.434 A 27.139 27.139 0 0 0 31.128 68.623 A 19.521 19.521 0 0 0 40.039 63.672 A 19.089 19.089 0 0 0 45.427 53.518 A 27.849 27.849 0 0 0 45.947 47.998 L 45.947 0 Z"
          vectorEffect="non-scaling-stroke"
        />
      </g>
      <g transform="matrix(-1,0,0,1,175,0)">
        <path
          className="kun-alphabet-path"
          fill="white"
          stroke="black"
          strokeWidth="4"
          d="M 51.221 0 L 51.221 71.094 L 48.584 71.094 L 2.686 4.492 L 2.686 71.094 L 0 71.094 L 0 0 L 2.686 0 L 48.584 66.455 L 48.584 0 L 51.221 0 Z"
          vectorEffect="non-scaling-stroke"
        />
      </g>
    </svg>
  );
});
