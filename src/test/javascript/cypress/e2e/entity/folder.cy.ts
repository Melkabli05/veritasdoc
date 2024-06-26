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

describe('Folder e2e test', () => {
  const folderPageUrl = '/folder';
  const folderPageUrlPattern = new RegExp('/folder(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const folderSample = { name: 'vice' };

  let folder;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/folders+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/folders').as('postEntityRequest');
    cy.intercept('DELETE', '/api/folders/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (folder) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/folders/${folder.id}`,
      }).then(() => {
        folder = undefined;
      });
    }
  });

  it('Folders menu should load Folders page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('folder');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Folder').should('exist');
    cy.url().should('match', folderPageUrlPattern);
  });

  describe('Folder page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(folderPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Folder page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/folder/new$'));
        cy.getEntityCreateUpdateHeading('Folder');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', folderPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/folders',
          body: folderSample,
        }).then(({ body }) => {
          folder = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/folders+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/folders?page=0&size=20>; rel="last",<http://localhost/api/folders?page=0&size=20>; rel="first"',
              },
              body: [folder],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(folderPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Folder page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('folder');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', folderPageUrlPattern);
      });

      it('edit button click should load edit Folder page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Folder');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', folderPageUrlPattern);
      });

      it('edit button click should load edit Folder page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Folder');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', folderPageUrlPattern);
      });

      it('last delete button click should delete instance of Folder', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('folder').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', folderPageUrlPattern);

        folder = undefined;
      });
    });
  });

  describe('new Folder page', () => {
    beforeEach(() => {
      cy.visit(`${folderPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Folder');
    });

    it('should create an instance of Folder', () => {
      cy.get(`[data-cy="name"]`).type('generally longingly');
      cy.get(`[data-cy="name"]`).should('have.value', 'generally longingly');

      cy.get(`[data-cy="parentFolderId"]`).type('above');
      cy.get(`[data-cy="parentFolderId"]`).should('have.value', 'above');

      cy.get(`[data-cy="createdAt"]`).type('2024-06-15T04:21');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2024-06-15T04:21');

      cy.get(`[data-cy="updatedAt"]`).type('2024-06-14T17:15');
      cy.get(`[data-cy="updatedAt"]`).blur();
      cy.get(`[data-cy="updatedAt"]`).should('have.value', '2024-06-14T17:15');

      cy.get(`[data-cy="isActive"]`).should('not.be.checked');
      cy.get(`[data-cy="isActive"]`).click();
      cy.get(`[data-cy="isActive"]`).should('be.checked');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        folder = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', folderPageUrlPattern);
    });
  });
});
