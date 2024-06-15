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

describe('FileVersion e2e test', () => {
  const fileVersionPageUrl = '/file-version';
  const fileVersionPageUrlPattern = new RegExp('/file-version(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const fileVersionSample = { fileId: 'unethically why', versionNumber: 18867, objectName: 'unethically processor properly' };

  let fileVersion;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/file-versions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/file-versions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/file-versions/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (fileVersion) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/file-versions/${fileVersion.id}`,
      }).then(() => {
        fileVersion = undefined;
      });
    }
  });

  it('FileVersions menu should load FileVersions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('file-version');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('FileVersion').should('exist');
    cy.url().should('match', fileVersionPageUrlPattern);
  });

  describe('FileVersion page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(fileVersionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create FileVersion page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/file-version/new$'));
        cy.getEntityCreateUpdateHeading('FileVersion');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', fileVersionPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/file-versions',
          body: fileVersionSample,
        }).then(({ body }) => {
          fileVersion = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/file-versions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/file-versions?page=0&size=20>; rel="last",<http://localhost/api/file-versions?page=0&size=20>; rel="first"',
              },
              body: [fileVersion],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(fileVersionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details FileVersion page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('fileVersion');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', fileVersionPageUrlPattern);
      });

      it('edit button click should load edit FileVersion page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('FileVersion');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', fileVersionPageUrlPattern);
      });

      it('edit button click should load edit FileVersion page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('FileVersion');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', fileVersionPageUrlPattern);
      });

      it('last delete button click should delete instance of FileVersion', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('fileVersion').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', fileVersionPageUrlPattern);

        fileVersion = undefined;
      });
    });
  });

  describe('new FileVersion page', () => {
    beforeEach(() => {
      cy.visit(`${fileVersionPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('FileVersion');
    });

    it('should create an instance of FileVersion', () => {
      cy.get(`[data-cy="fileId"]`).type('boom blah since');
      cy.get(`[data-cy="fileId"]`).should('have.value', 'boom blah since');

      cy.get(`[data-cy="versionNumber"]`).type('30940');
      cy.get(`[data-cy="versionNumber"]`).should('have.value', '30940');

      cy.get(`[data-cy="objectName"]`).type('chime safely');
      cy.get(`[data-cy="objectName"]`).should('have.value', 'chime safely');

      cy.get(`[data-cy="createdAt"]`).type('2024-06-14T17:18');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2024-06-14T17:18');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        fileVersion = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', fileVersionPageUrlPattern);
    });
  });
});
