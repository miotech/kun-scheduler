import React from "react";
import c from "clsx";

import css from "./ImgBackContainer.module.css";

export default function ImgBackContainer({ className, img }) {
  return (
    <div
      className={c(css.ImgBackContainer, className)}
      style={{ backgroundImage: `url(${img})` }}
    />
  );
}
