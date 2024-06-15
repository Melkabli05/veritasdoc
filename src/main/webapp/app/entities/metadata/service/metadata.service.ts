import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { map, Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IMetadata, NewMetadata } from '../metadata.model';

export type PartialUpdateMetadata = Partial<IMetadata> & Pick<IMetadata, 'id'>;

type RestOf<T extends IMetadata | NewMetadata> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type RestMetadata = RestOf<IMetadata>;

export type NewRestMetadata = RestOf<NewMetadata>;

export type PartialUpdateRestMetadata = RestOf<PartialUpdateMetadata>;

export type EntityResponseType = HttpResponse<IMetadata>;
export type EntityArrayResponseType = HttpResponse<IMetadata[]>;

@Injectable({ providedIn: 'root' })
export class MetadataService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/metadata');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/metadata/_search');

  create(metadata: NewMetadata): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(metadata);
    return this.http
      .post<RestMetadata>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(metadata: IMetadata): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(metadata);
    return this.http
      .put<RestMetadata>(`${this.resourceUrl}/${this.getMetadataIdentifier(metadata)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(metadata: PartialUpdateMetadata): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(metadata);
    return this.http
      .patch<RestMetadata>(`${this.resourceUrl}/${this.getMetadataIdentifier(metadata)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestMetadata>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestMetadata[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestMetadata[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),

      catchError(() => scheduled([new HttpResponse<IMetadata[]>()], asapScheduler)),
    );
  }

  getMetadataIdentifier(metadata: Pick<IMetadata, 'id'>): number {
    return metadata.id;
  }

  compareMetadata(o1: Pick<IMetadata, 'id'> | null, o2: Pick<IMetadata, 'id'> | null): boolean {
    return o1 && o2 ? this.getMetadataIdentifier(o1) === this.getMetadataIdentifier(o2) : o1 === o2;
  }

  addMetadataToCollectionIfMissing<Type extends Pick<IMetadata, 'id'>>(
    metadataCollection: Type[],
    ...metadataToCheck: (Type | null | undefined)[]
  ): Type[] {
    const metadata: Type[] = metadataToCheck.filter(isPresent);
    if (metadata.length > 0) {
      const metadataCollectionIdentifiers = metadataCollection.map(metadataItem => this.getMetadataIdentifier(metadataItem));
      const metadataToAdd = metadata.filter(metadataItem => {
        const metadataIdentifier = this.getMetadataIdentifier(metadataItem);
        if (metadataCollectionIdentifiers.includes(metadataIdentifier)) {
          return false;
        }
        metadataCollectionIdentifiers.push(metadataIdentifier);
        return true;
      });
      return [...metadataToAdd, ...metadataCollection];
    }
    return metadataCollection;
  }

  protected convertDateFromClient<T extends IMetadata | NewMetadata | PartialUpdateMetadata>(metadata: T): RestOf<T> {
    return {
      ...metadata,
      createdAt: metadata.createdAt?.toJSON() ?? null,
      updatedAt: metadata.updatedAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restMetadata: RestMetadata): IMetadata {
    return {
      ...restMetadata,
      createdAt: restMetadata.createdAt ? dayjs(restMetadata.createdAt) : undefined,
      updatedAt: restMetadata.updatedAt ? dayjs(restMetadata.updatedAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestMetadata>): HttpResponse<IMetadata> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestMetadata[]>): HttpResponse<IMetadata[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
