import React from "react";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";
import useBaseUrl from "@docusaurus/useBaseUrl";

import css from "./Function.module.css";

export default function Function() {
  const functionIcon1 = useBaseUrl("/img/function-icon1.png");
  const functionIcon2 = useBaseUrl("/img/function-icon2.png");
  const functionIcon3 = useBaseUrl("/img/function-icon3.png");
  const functionIcon4 = useBaseUrl("/img/function-icon4.png");

  return (
    <div className={css.Function}>
      <div className={css.title}>功能</div>
      <div className={css.cardContainer}>
        <div className={css.card}>
          <ImgBackContainer className={css.cardImg} img={functionIcon1} />
          <div className={css.cardTitle}>面向数据治理</div>
          <div className={css.cardSubTitle}>
            同等关注数据与元数据，自带数据发现、数据质量、数据血缘功能，便于进行数据治理。
          </div>
        </div>
        <div className={css.card}>
          <ImgBackContainer className={css.cardImg} img={functionIcon2} />
          <div className={css.cardTitle}>简单易用</div>
          <div className={css.cardSubTitle}>
            在Web
            UI上可以完成所有操作，如定义DAG、监控状态、操作任务、补数据等。
          </div>
        </div>
        <div className={css.card}>
          <ImgBackContainer className={css.cardImg} img={functionIcon3} />
          <div className={css.cardTitle}>水平扩容</div>
          <div className={css.cardSubTitle}>
            支持Local、Kubernetes等多种调度引擎，可以根据业务负载容易地水平扩容。
          </div>
        </div>
        <div className={css.card}>
          <ImgBackContainer className={css.cardImg} img={functionIcon4} />
          <div className={css.cardTitle}>可扩展</div>
          <div className={css.cardSubTitle}>
            用户可以增加新的任务类型，新的计算引擎，新的存储系统，新的执行引擎等。
          </div>
        </div>
      </div>
    </div>
  );
}
