import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";
import SectionDescContent from "../SectionDescContent/SectionDescContent";

import css from "./DataTask.module.css";

export default function DataTask() {
  const imgUrl = useBaseUrl("/img/home-figure1.png");

  return (
    <div className={css.DataTask}>
      <ImgBackContainer className={css.img} img={imgUrl} />
      <SectionDescContent
        title="数据任务"
        subTitle="Kun内置有多种不同的任务类型。你可以在UI上面创建数据任务，对任务进行试跑和调试，并且发布到生产环境。"
      />
    </div>
  );
}
