/// <reference types="cypress" />

interface LoginOptions {
  username?: string;
  password?: string;
  targetUrl?: string;
}

declare namespace Cypress {
  interface Chainable {
    /**
     * Login user by using UI
     * @param username
     * @param password
     */
    login(options?: LoginOptions): void;

    /**
     * Find element by date-testid attribute
     * @param dataTestAttribute
     */
    getEleBySelector(dataTestAttribute: string, args?: any): Chainable<JQuery<HTMLElement>>;
    visualSnapshot(maybeName: string): void;
  }
}
