import React from 'react';

const SimpleErrorFallbackComponent: React.FC<any> = ({ componentStack, error }) => {
  return (
    <div>
      <p>
        <strong>Oops! An error occured!</strong>
      </p>
      <p>Here’s what we know…</p>
      <p>
        <strong>Error:</strong> {error.toString()}
      </p>
      <p>
        <strong>Stacktrace:</strong>
        <pre>{componentStack}</pre>
      </p>
    </div>
  );
};

export default SimpleErrorFallbackComponent;
