import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'file',
    data: { pageTitle: 'veritasdocApp.file.home.title' },
    loadChildren: () => import('./file/file.routes'),
  },
  {
    path: 'file-version',
    data: { pageTitle: 'veritasdocApp.fileVersion.home.title' },
    loadChildren: () => import('./file-version/file-version.routes'),
  },
  {
    path: 'metadata',
    data: { pageTitle: 'veritasdocApp.metadata.home.title' },
    loadChildren: () => import('./metadata/metadata.routes'),
  },
  {
    path: 'tag',
    data: { pageTitle: 'veritasdocApp.tag.home.title' },
    loadChildren: () => import('./tag/tag.routes'),
  },
  {
    path: 'permission',
    data: { pageTitle: 'veritasdocApp.permission.home.title' },
    loadChildren: () => import('./permission/permission.routes'),
  },
  {
    path: 'folder',
    data: { pageTitle: 'veritasdocApp.folder.home.title' },
    loadChildren: () => import('./folder/folder.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
