import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IFileVersion } from '../file-version.model';
import { FileVersionService } from '../service/file-version.service';

const fileVersionResolve = (route: ActivatedRouteSnapshot): Observable<null | IFileVersion> => {
  const id = route.params['id'];
  if (id) {
    return inject(FileVersionService)
      .find(id)
      .pipe(
        mergeMap((fileVersion: HttpResponse<IFileVersion>) => {
          if (fileVersion.body) {
            return of(fileVersion.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default fileVersionResolve;
