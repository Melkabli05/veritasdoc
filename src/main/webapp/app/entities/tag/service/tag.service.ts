import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { map, Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { ITag, NewTag } from '../tag.model';

export type PartialUpdateTag = Partial<ITag> & Pick<ITag, 'id'>;

type RestOf<T extends ITag | NewTag> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type RestTag = RestOf<ITag>;

export type NewRestTag = RestOf<NewTag>;

export type PartialUpdateRestTag = RestOf<PartialUpdateTag>;

export type EntityResponseType = HttpResponse<ITag>;
export type EntityArrayResponseType = HttpResponse<ITag[]>;

@Injectable({ providedIn: 'root' })
export class TagService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/tags');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/tags/_search');

  create(tag: NewTag): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(tag);
    return this.http.post<RestTag>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(tag: ITag): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(tag);
    return this.http
      .put<RestTag>(`${this.resourceUrl}/${this.getTagIdentifier(tag)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(tag: PartialUpdateTag): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(tag);
    return this.http
      .patch<RestTag>(`${this.resourceUrl}/${this.getTagIdentifier(tag)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestTag>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestTag[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestTag[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),

      catchError(() => scheduled([new HttpResponse<ITag[]>()], asapScheduler)),
    );
  }

  getTagIdentifier(tag: Pick<ITag, 'id'>): number {
    return tag.id;
  }

  compareTag(o1: Pick<ITag, 'id'> | null, o2: Pick<ITag, 'id'> | null): boolean {
    return o1 && o2 ? this.getTagIdentifier(o1) === this.getTagIdentifier(o2) : o1 === o2;
  }

  addTagToCollectionIfMissing<Type extends Pick<ITag, 'id'>>(tagCollection: Type[], ...tagsToCheck: (Type | null | undefined)[]): Type[] {
    const tags: Type[] = tagsToCheck.filter(isPresent);
    if (tags.length > 0) {
      const tagCollectionIdentifiers = tagCollection.map(tagItem => this.getTagIdentifier(tagItem));
      const tagsToAdd = tags.filter(tagItem => {
        const tagIdentifier = this.getTagIdentifier(tagItem);
        if (tagCollectionIdentifiers.includes(tagIdentifier)) {
          return false;
        }
        tagCollectionIdentifiers.push(tagIdentifier);
        return true;
      });
      return [...tagsToAdd, ...tagCollection];
    }
    return tagCollection;
  }

  protected convertDateFromClient<T extends ITag | NewTag | PartialUpdateTag>(tag: T): RestOf<T> {
    return {
      ...tag,
      createdAt: tag.createdAt?.toJSON() ?? null,
      updatedAt: tag.updatedAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restTag: RestTag): ITag {
    return {
      ...restTag,
      createdAt: restTag.createdAt ? dayjs(restTag.createdAt) : undefined,
      updatedAt: restTag.updatedAt ? dayjs(restTag.updatedAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestTag>): HttpResponse<ITag> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestTag[]>): HttpResponse<ITag[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
