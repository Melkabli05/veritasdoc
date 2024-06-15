import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { FileVersionComponent } from './list/file-version.component';
import { FileVersionDetailComponent } from './detail/file-version-detail.component';
import { FileVersionUpdateComponent } from './update/file-version-update.component';
import FileVersionResolve from './route/file-version-routing-resolve.service';

const fileVersionRoute: Routes = [
  {
    path: '',
    component: FileVersionComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: FileVersionDetailComponent,
    resolve: {
      fileVersion: FileVersionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: FileVersionUpdateComponent,
    resolve: {
      fileVersion: FileVersionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: FileVersionUpdateComponent,
    resolve: {
      fileVersion: FileVersionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default fileVersionRoute;
