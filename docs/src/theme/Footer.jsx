import React from "react";
import c from 'clsx';
import useBaseUrl from "@docusaurus/useBaseUrl";
import ImgBackContainer from '../components/ImgBackContainer/ImgBackContainer';

import css from "./Footer.module.css";

export default function Footer() {
  const imgUrl = useBaseUrl("/img/logo-white.png");

  return (
    <div className={css.Footer}>
      <ImgBackContainer className={c(css.leftItem, css.logo)} img={imgUrl} />
    </div>
  );
}
