describe('User Login', () => {
  it('should redirect unauthenticated user to login page', () => {
    cy.visit('/');
    cy.wait(500).then(() => {
      cy.location('pathname').should('equal', '/login');
      cy.visualSnapshot('Redirect to Login Page');
    });
  });
  it('should redirect to the home page after login', () => {
    cy.login();
    cy.location('pathname').should('equal', '/monitoring-dashboard');
  });
});
