import React, { memo } from "react";

import css from "./Copyright.module.css";

export default memo(function Copyright() {
  return (
    <div className={css.Copyright}>
      <span>沪ICP备17028310号-2</span>
    </div>
  );
});
