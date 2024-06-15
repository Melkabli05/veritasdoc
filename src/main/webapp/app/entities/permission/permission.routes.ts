import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { PermissionComponent } from './list/permission.component';
import { PermissionDetailComponent } from './detail/permission-detail.component';
import { PermissionUpdateComponent } from './update/permission-update.component';
import PermissionResolve from './route/permission-routing-resolve.service';

const permissionRoute: Routes = [
  {
    path: '',
    component: PermissionComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: PermissionDetailComponent,
    resolve: {
      permission: PermissionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: PermissionUpdateComponent,
    resolve: {
      permission: PermissionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: PermissionUpdateComponent,
    resolve: {
      permission: PermissionResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default permissionRoute;
