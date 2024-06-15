import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { map, Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IFileVersion, NewFileVersion } from '../file-version.model';

export type PartialUpdateFileVersion = Partial<IFileVersion> & Pick<IFileVersion, 'id'>;

type RestOf<T extends IFileVersion | NewFileVersion> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestFileVersion = RestOf<IFileVersion>;

export type NewRestFileVersion = RestOf<NewFileVersion>;

export type PartialUpdateRestFileVersion = RestOf<PartialUpdateFileVersion>;

export type EntityResponseType = HttpResponse<IFileVersion>;
export type EntityArrayResponseType = HttpResponse<IFileVersion[]>;

@Injectable({ providedIn: 'root' })
export class FileVersionService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/file-versions');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/file-versions/_search');

  create(fileVersion: NewFileVersion): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(fileVersion);
    return this.http
      .post<RestFileVersion>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(fileVersion: IFileVersion): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(fileVersion);
    return this.http
      .put<RestFileVersion>(`${this.resourceUrl}/${this.getFileVersionIdentifier(fileVersion)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(fileVersion: PartialUpdateFileVersion): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(fileVersion);
    return this.http
      .patch<RestFileVersion>(`${this.resourceUrl}/${this.getFileVersionIdentifier(fileVersion)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestFileVersion>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestFileVersion[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestFileVersion[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),

      catchError(() => scheduled([new HttpResponse<IFileVersion[]>()], asapScheduler)),
    );
  }

  getFileVersionIdentifier(fileVersion: Pick<IFileVersion, 'id'>): number {
    return fileVersion.id;
  }

  compareFileVersion(o1: Pick<IFileVersion, 'id'> | null, o2: Pick<IFileVersion, 'id'> | null): boolean {
    return o1 && o2 ? this.getFileVersionIdentifier(o1) === this.getFileVersionIdentifier(o2) : o1 === o2;
  }

  addFileVersionToCollectionIfMissing<Type extends Pick<IFileVersion, 'id'>>(
    fileVersionCollection: Type[],
    ...fileVersionsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const fileVersions: Type[] = fileVersionsToCheck.filter(isPresent);
    if (fileVersions.length > 0) {
      const fileVersionCollectionIdentifiers = fileVersionCollection.map(fileVersionItem => this.getFileVersionIdentifier(fileVersionItem));
      const fileVersionsToAdd = fileVersions.filter(fileVersionItem => {
        const fileVersionIdentifier = this.getFileVersionIdentifier(fileVersionItem);
        if (fileVersionCollectionIdentifiers.includes(fileVersionIdentifier)) {
          return false;
        }
        fileVersionCollectionIdentifiers.push(fileVersionIdentifier);
        return true;
      });
      return [...fileVersionsToAdd, ...fileVersionCollection];
    }
    return fileVersionCollection;
  }

  protected convertDateFromClient<T extends IFileVersion | NewFileVersion | PartialUpdateFileVersion>(fileVersion: T): RestOf<T> {
    return {
      ...fileVersion,
      createdAt: fileVersion.createdAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restFileVersion: RestFileVersion): IFileVersion {
    return {
      ...restFileVersion,
      createdAt: restFileVersion.createdAt ? dayjs(restFileVersion.createdAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestFileVersion>): HttpResponse<IFileVersion> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestFileVersion[]>): HttpResponse<IFileVersion[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
