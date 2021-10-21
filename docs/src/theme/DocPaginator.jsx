import React from "react";
import c from "clsx";
import Link from "@docusaurus/Link";
import css from "./DocPaginator.module.css";

function DocPaginator({ metadata }) {
  return (
    <nav className={css.DocPaginator} aria-label="Blog list page navigation">
      <div className={css.paginationNavItem}>
        {metadata.previous && (
          <Link
            className={css.paginationNavLink}
            to={metadata.previous.permalink}
          >
            <div className={css.directionIcon} style={{ marginRight: 14 }}>
              {"<"}
            </div>
            <div className={css.previousTitleContent}>
              <div className={css.paginationNavSublabel}>Previous</div>
              <div className={css.paginationNavLabel}>
                {metadata.previous.title}
              </div>
            </div>
          </Link>
        )}
      </div>
      <div className={c(css.paginationNavItem, css.paginationNavItemNext)}>
        {metadata.next && (
          <Link className={css.paginationNavLink} to={metadata.next.permalink}>
            <div className={css.nextTitleContent}>
              <div className={css.paginationNavSublabel}>Next</div>
              <div className={css.paginationNavLabel}>
                {metadata.next.title}
              </div>
            </div>
            <div className={css.directionIcon} style={{ marginLeft: 14 }}>
              {">"}
            </div>
          </Link>
        )}
      </div>
    </nav>
  );
}

export default DocPaginator;
