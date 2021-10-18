import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";

import css from "./FunctionPrinciple.module.css";

export default function FunctionPrinciple() {
  const imgUrl = useBaseUrl("/img/workflow.png");

  return (
    <div className={css.FunctionPrinciple}>
      <div className={css.title}>工作原理</div>
      <ImgBackContainer className={css.img} img={imgUrl} />
    </div>
  );
}
