import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";
import SectionDescContent from "../SectionDescContent/SectionDescContent";

import css from "./DataTable.module.css";

export default function DataTable() {
  const imgUrl = useBaseUrl("/img/home-figure3.png");

  return (
    <div className={css.DataTable}>
      <ImgBackContainer className={css.img} img={imgUrl} />
      <SectionDescContent
        title="数据表"
        subTitle="Kun会收集数据仓库中所有表的信息，包括schema和统计信息。支持定期同步与基于事件的同步。"
      />
    </div>
  );
}
