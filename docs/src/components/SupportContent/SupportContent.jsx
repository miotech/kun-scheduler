import React from "react";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";
import useBaseUrl from "@docusaurus/useBaseUrl";
import css from "./SupportContent.module.css";

export default function SupportContent() {
  const supportImg1 = useBaseUrl("/img/elasticsearch.png");
  const supportImg2 = useBaseUrl("/img/hive.png");
  const supportImg3 = useBaseUrl("/img/mongoDB.png");
  const supportImg4 = useBaseUrl("/img/mysql.png");
  const supportImg5 = useBaseUrl("/img/spark.png");
  return (
    <div className={css.SupportContent}>
      <div className={css.title}>支持</div>

      <div className={css.imgContainer}>
        <ImgBackContainer className={css.img} img={supportImg1} />
        <ImgBackContainer className={css.img} img={supportImg2} />
        <ImgBackContainer className={css.img} img={supportImg3} />
        <ImgBackContainer className={css.img} img={supportImg4} />
        <ImgBackContainer className={css.img} img={supportImg5} />
      </div>
    </div>
  );
}
