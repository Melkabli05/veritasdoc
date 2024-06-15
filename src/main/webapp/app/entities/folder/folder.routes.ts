import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { FolderComponent } from './list/folder.component';
import { FolderDetailComponent } from './detail/folder-detail.component';
import { FolderUpdateComponent } from './update/folder-update.component';
import FolderResolve from './route/folder-routing-resolve.service';

const folderRoute: Routes = [
  {
    path: '',
    component: FolderComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: FolderDetailComponent,
    resolve: {
      folder: FolderResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: FolderUpdateComponent,
    resolve: {
      folder: FolderResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: FolderUpdateComponent,
    resolve: {
      folder: FolderResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default folderRoute;
