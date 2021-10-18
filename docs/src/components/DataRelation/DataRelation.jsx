import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";
import SectionDescContent from "../SectionDescContent/SectionDescContent";

import css from "./DataRelation.module.css";

export default function DataRelation() {
  const imgUrl = useBaseUrl("/img/home-figure4.png");
  return (
    <div className={css.DataRelation}>
      <SectionDescContent
        title="数据血缘"
        subTitle="Kun会根据任务内容，自动推导出数据血缘，而无需任何额外工作量。"
      />
      <ImgBackContainer className={css.img} img={imgUrl} />
    </div>
  );
}
