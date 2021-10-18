import React from "react";
import NavBar from "@theme/NavBar";
import ThemeProvider from "@theme/ThemeProvider";
import UserPreferencesProvider from "@theme/UserPreferencesProvider";
import Footer from "./Footer";

import css from "./Layout.module.css";

function Providers({ children }) {
  return (
    <ThemeProvider>
      <UserPreferencesProvider>{children}</UserPreferencesProvider>
    </ThemeProvider>
  );
}

export default function Layout({ children }) {
  return (
    <Providers>
      <div className={css.Layout}>
        <NavBar />
        <div className={css.contentWrapper}>
          <div>{children}</div>
          <Footer />
        </div>
      </div>
    </Providers>
  );
}
