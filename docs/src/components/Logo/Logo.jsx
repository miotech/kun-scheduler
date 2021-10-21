import React from "react";
import c from "clsx";
import useBaseUrl from "@docusaurus/useBaseUrl";

import css from "./Logo.module.css";

export default function Logo({ className }) {
  const logoUrl = useBaseUrl("/img/kun-logo.svg");
  return (
    <div
      className={c(css.Logo, className)}
      style={{ backgroundImage: `url(${logoUrl})` }}
    />
  );
}
