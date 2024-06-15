import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Metadata e2e test', () => {
  const metadataPageUrl = '/metadata';
  const metadataPageUrlPattern = new RegExp('/metadata(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const metadataSample = { fileId: 'finally ankle excluding', key: 'motive', value: 'expunge zowie carelessly' };

  let metadata;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/metadata+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/metadata').as('postEntityRequest');
    cy.intercept('DELETE', '/api/metadata/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (metadata) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/metadata/${metadata.id}`,
      }).then(() => {
        metadata = undefined;
      });
    }
  });

  it('Metadata menu should load Metadata page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('metadata');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Metadata').should('exist');
    cy.url().should('match', metadataPageUrlPattern);
  });

  describe('Metadata page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(metadataPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Metadata page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/metadata/new$'));
        cy.getEntityCreateUpdateHeading('Metadata');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', metadataPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/metadata',
          body: metadataSample,
        }).then(({ body }) => {
          metadata = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/metadata+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/metadata?page=0&size=20>; rel="last",<http://localhost/api/metadata?page=0&size=20>; rel="first"',
              },
              body: [metadata],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(metadataPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Metadata page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('metadata');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', metadataPageUrlPattern);
      });

      it('edit button click should load edit Metadata page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Metadata');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', metadataPageUrlPattern);
      });

      it('edit button click should load edit Metadata page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Metadata');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', metadataPageUrlPattern);
      });

      it('last delete button click should delete instance of Metadata', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('metadata').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', metadataPageUrlPattern);

        metadata = undefined;
      });
    });
  });

  describe('new Metadata page', () => {
    beforeEach(() => {
      cy.visit(`${metadataPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Metadata');
    });

    it('should create an instance of Metadata', () => {
      cy.get(`[data-cy="fileId"]`).type('prejudice fair');
      cy.get(`[data-cy="fileId"]`).should('have.value', 'prejudice fair');

      cy.get(`[data-cy="key"]`).type('in condemn persecute');
      cy.get(`[data-cy="key"]`).should('have.value', 'in condemn persecute');

      cy.get(`[data-cy="value"]`).type('of besides');
      cy.get(`[data-cy="value"]`).should('have.value', 'of besides');

      cy.get(`[data-cy="createdAt"]`).type('2024-06-15T00:43');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2024-06-15T00:43');

      cy.get(`[data-cy="updatedAt"]`).type('2024-06-14T16:13');
      cy.get(`[data-cy="updatedAt"]`).blur();
      cy.get(`[data-cy="updatedAt"]`).should('have.value', '2024-06-14T16:13');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        metadata = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', metadataPageUrlPattern);
    });
  });
});
