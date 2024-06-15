import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { FileComponent } from './list/file.component';
import { FileDetailComponent } from './detail/file-detail.component';
import { FileUpdateComponent } from './update/file-update.component';
import FileResolve from './route/file-routing-resolve.service';

const fileRoute: Routes = [
  {
    path: '',
    component: FileComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: FileDetailComponent,
    resolve: {
      file: FileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: FileUpdateComponent,
    resolve: {
      file: FileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: FileUpdateComponent,
    resolve: {
      file: FileResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default fileRoute;
