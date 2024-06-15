import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { map, Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IFolder, NewFolder } from '../folder.model';

export type PartialUpdateFolder = Partial<IFolder> & Pick<IFolder, 'id'>;

type RestOf<T extends IFolder | NewFolder> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type RestFolder = RestOf<IFolder>;

export type NewRestFolder = RestOf<NewFolder>;

export type PartialUpdateRestFolder = RestOf<PartialUpdateFolder>;

export type EntityResponseType = HttpResponse<IFolder>;
export type EntityArrayResponseType = HttpResponse<IFolder[]>;

@Injectable({ providedIn: 'root' })
export class FolderService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/folders');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/folders/_search');

  create(folder: NewFolder): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(folder);
    return this.http
      .post<RestFolder>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(folder: IFolder): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(folder);
    return this.http
      .put<RestFolder>(`${this.resourceUrl}/${this.getFolderIdentifier(folder)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(folder: PartialUpdateFolder): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(folder);
    return this.http
      .patch<RestFolder>(`${this.resourceUrl}/${this.getFolderIdentifier(folder)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestFolder>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestFolder[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestFolder[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),

      catchError(() => scheduled([new HttpResponse<IFolder[]>()], asapScheduler)),
    );
  }

  getFolderIdentifier(folder: Pick<IFolder, 'id'>): number {
    return folder.id;
  }

  compareFolder(o1: Pick<IFolder, 'id'> | null, o2: Pick<IFolder, 'id'> | null): boolean {
    return o1 && o2 ? this.getFolderIdentifier(o1) === this.getFolderIdentifier(o2) : o1 === o2;
  }

  addFolderToCollectionIfMissing<Type extends Pick<IFolder, 'id'>>(
    folderCollection: Type[],
    ...foldersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const folders: Type[] = foldersToCheck.filter(isPresent);
    if (folders.length > 0) {
      const folderCollectionIdentifiers = folderCollection.map(folderItem => this.getFolderIdentifier(folderItem));
      const foldersToAdd = folders.filter(folderItem => {
        const folderIdentifier = this.getFolderIdentifier(folderItem);
        if (folderCollectionIdentifiers.includes(folderIdentifier)) {
          return false;
        }
        folderCollectionIdentifiers.push(folderIdentifier);
        return true;
      });
      return [...foldersToAdd, ...folderCollection];
    }
    return folderCollection;
  }

  protected convertDateFromClient<T extends IFolder | NewFolder | PartialUpdateFolder>(folder: T): RestOf<T> {
    return {
      ...folder,
      createdAt: folder.createdAt?.toJSON() ?? null,
      updatedAt: folder.updatedAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restFolder: RestFolder): IFolder {
    return {
      ...restFolder,
      createdAt: restFolder.createdAt ? dayjs(restFolder.createdAt) : undefined,
      updatedAt: restFolder.updatedAt ? dayjs(restFolder.updatedAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestFolder>): HttpResponse<IFolder> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestFolder[]>): HttpResponse<IFolder[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
