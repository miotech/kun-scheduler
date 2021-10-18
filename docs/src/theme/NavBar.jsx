import React from "react";
import useBaseUrl from "@docusaurus/useBaseUrl";
import Link from "@docusaurus/Link";
import Logo from "../components/Logo/Logo";
import NavPageContainer from "../components/NavPageContainer/NavPageContainer";
import css from "./NavBar.module.css";

export default function NavBar() {
  return (
    <div className={css.NavBar}>
      <Link to={useBaseUrl("/")}>
        <Logo />
      </Link>
      <NavPageContainer />
      <div className={css.buttonContainer}>
        <Link className={css.startButton} to={useBaseUrl("docs/")}>
          <span className={css.startButtonText}>立即开始</span>
        </Link>
      </div>
    </div>
  );
}
