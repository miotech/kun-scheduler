import React from "react";
import clsx from "clsx";
import Layout from "@theme/Layout";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import useBaseUrl from "@docusaurus/useBaseUrl";
import Banner from "../components/Banner/Banner";
import Function from "../components/Function/Function";
import FunctionPrinciple from "../components/FunctionPrinciple/FunctionPrinciple";
import DataTask from "../components/DataTask/DataTask";
import TaskDAG from "../components/TaskDAG/TaskDAG";
import DataTable from "../components/DataTable/DataTable";
import DataRelation from "../components/DataRelation/DataRelation";
import SupportContent from "../components/SupportContent/SupportContent";

import css from "./styles.module.css";

function Feature({ imageUrl, title, description }) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx("col col--4", css.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={css.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

function Home() {
  const context = useDocusaurusContext();
  const { siteConfig = {} } = context;
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Description will go into a meta tag in <head />"
    >
      <header className={css.container}>
        <Banner />
      </header>
      <main>
        <div className={css.colorContent}>
          <div className={css.colorContentInner}>
            <Function />
          </div>
        </div>
        <div className={css.container}>
          <FunctionPrinciple />
        </div>
        <div className={css.container}>
          <DataTask />
        </div>
        <div className={css.container}>
          <TaskDAG />
        </div>
        <div className={css.container}>
          <DataTable />
        </div>
        <div className={css.container}>
          <DataRelation />
        </div>
        <div className={css.colorContent}>
          <div className={css.colorContentInner}>
            <SupportContent />
          </div>
        </div>
      </main>
    </Layout>
  );
}

export default Home;
