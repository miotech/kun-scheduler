onmessage = function onSparkSqlMessage(event) {
  postMessage({
    event,
  }, '');
};
