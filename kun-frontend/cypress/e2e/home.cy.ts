describe('Home Page', () => {
  beforeEach(() => {
    cy.login();
  });
  it('should load in home page', () => {
    cy.location('pathname').should('equal', '/monitoring-dashboard');
  });
});
