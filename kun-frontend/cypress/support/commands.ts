/// <reference path="../global.d.ts"/>

// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
// declare global {
//   namespace Cypress {
//     interface Chainable {
//       login(email: string, password: string): Chainable<void>
//       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
//     }
//   }
// }

import '@percy/cypress';

Cypress.Commands.add('login', options => {
  const { AUTH_PASSWORD, AUTH_USERNAME } = Cypress.env();
  const { username = AUTH_USERNAME, password = AUTH_PASSWORD, targetUrl } = options || {};
  const loginPath = '/login';
  const log = Cypress.log({
    name: 'login',
    displayName: 'LOGIN',
    message: [`🔐 Authenticating | ${username}`],
    autoEnd: false,
  });

  cy.intercept('POST', '/kun/api/v1/security/login').as('loginUser');

  cy.location('pathname', { log: false }).then(currentPath => {
    if (currentPath !== loginPath) {
      cy.visit(loginPath);
    }
  });

  log.snapshot('before');

  cy.getEleBySelector('username').type(username);
  cy.getEleBySelector('password').type(password);
  cy.getEleBySelector('login').click();

  cy.wait('@loginUser').then(() => {
    cy.visit(targetUrl || '/');
    log.snapshot('after');
    log.snapshot('end');
  });
});

Cypress.Commands.add('getEleBySelector', (selector, ...args) => {
  return cy.get(`[data-testid=${selector}]`, ...args);
});

Cypress.Commands.add('visualSnapshot', maybeName => {
  // @ts-ignore
  let snapshotTitle = cy.state('runnable').fullTitle();
  if (maybeName) {
    snapshotTitle = `${snapshotTitle} - ${maybeName}`;
  }
  cy.percySnapshot(snapshotTitle, {
    // @ts-ignore
    widths: [cy.state('viewportWidth')],
    // @ts-ignore
    minHeight: cy.state('viewportHeight'),
  });
});
