import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";
import SectionDescContent from "../SectionDescContent/SectionDescContent";

import css from "./TaskDAG.module.css";

export default function TaskDAG() {
  const imgUrl = useBaseUrl("/img/home-figure2.png");
  return (
    <div className={css.TaskDAG}>
      <SectionDescContent
        title="任务DAG"
        subTitle="在Kun上，你可以通过UI编辑复杂DAG，并且查看每天的任务运行结果。"
      />
      <ImgBackContainer className={css.img} img={imgUrl} />
    </div>
  );
}
