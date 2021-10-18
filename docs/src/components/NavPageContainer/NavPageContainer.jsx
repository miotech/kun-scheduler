import React from "react";
import Link from '@docusaurus/Link';
import css from "./NavPageContainer.module.css";

export default function NavPageContainer() {
  return (
    <div className={css.NavPageContainer}>
      <Link to="/docs" className={css.NavPageItem}>
        文档
      </Link>
      <Link
        to="https://github.com/miotech/KUN"
        target="_blank"
        className={css.NavPageItem}
      >
        社区
      </Link>
    </div>
  );
}
