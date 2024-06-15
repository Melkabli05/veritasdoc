import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IFile } from '../file.model';
import { FileService } from '../service/file.service';

const fileResolve = (route: ActivatedRouteSnapshot): Observable<null | IFile> => {
  const id = route.params['id'];
  if (id) {
    return inject(FileService)
      .find(id)
      .pipe(
        mergeMap((file: HttpResponse<IFile>) => {
          if (file.body) {
            return of(file.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default fileResolve;
