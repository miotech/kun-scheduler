import React from "react";
import Link from "@docusaurus/Link";
import Logo from "../Logo/Logo";
import ImgBackContainer from "../ImgBackContainer/ImgBackContainer";
import useBaseUrl from "@docusaurus/useBaseUrl";

import css from "./Banner.module.css";

export default function Banner() {
  const logoUrl = useBaseUrl("/img/main-figure.png");

  return (
    <div className={css.Banner}>
      <div className={css.BannerContent}>
        <Logo className={css.Logo} />
        <div className={css.title}>
          超越了工作流调度的
          <br />
          一站式大数据开发平台
        </div>
        <Link to={useBaseUrl("docs/")} className={css.begin}>
          立即开始
        </Link>
      </div>
      <ImgBackContainer className={css.bannerImg} img={logoUrl} />
    </div>
  );
}
