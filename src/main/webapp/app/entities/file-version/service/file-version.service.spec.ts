import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IFileVersion } from '../file-version.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../file-version.test-samples';

import { FileVersionService, RestFileVersion } from './file-version.service';

const requireRestSample: RestFileVersion = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
};

describe('FileVersion Service', () => {
  let service: FileVersionService;
  let httpMock: HttpTestingController;
  let expectedResult: IFileVersion | IFileVersion[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(FileVersionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a FileVersion', () => {
      const fileVersion = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(fileVersion).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a FileVersion', () => {
      const fileVersion = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(fileVersion).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a FileVersion', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of FileVersion', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a FileVersion', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a FileVersion', () => {
      const queryObject: any = {
        page: 0,
        size: 20,
        query: '',
        sort: [],
      };
      service.search(queryObject).subscribe(() => expectedResult);

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
      expect(expectedResult).toBe(null);
    });

    describe('addFileVersionToCollectionIfMissing', () => {
      it('should add a FileVersion to an empty array', () => {
        const fileVersion: IFileVersion = sampleWithRequiredData;
        expectedResult = service.addFileVersionToCollectionIfMissing([], fileVersion);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(fileVersion);
      });

      it('should not add a FileVersion to an array that contains it', () => {
        const fileVersion: IFileVersion = sampleWithRequiredData;
        const fileVersionCollection: IFileVersion[] = [
          {
            ...fileVersion,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addFileVersionToCollectionIfMissing(fileVersionCollection, fileVersion);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a FileVersion to an array that doesn't contain it", () => {
        const fileVersion: IFileVersion = sampleWithRequiredData;
        const fileVersionCollection: IFileVersion[] = [sampleWithPartialData];
        expectedResult = service.addFileVersionToCollectionIfMissing(fileVersionCollection, fileVersion);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(fileVersion);
      });

      it('should add only unique FileVersion to an array', () => {
        const fileVersionArray: IFileVersion[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const fileVersionCollection: IFileVersion[] = [sampleWithRequiredData];
        expectedResult = service.addFileVersionToCollectionIfMissing(fileVersionCollection, ...fileVersionArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const fileVersion: IFileVersion = sampleWithRequiredData;
        const fileVersion2: IFileVersion = sampleWithPartialData;
        expectedResult = service.addFileVersionToCollectionIfMissing([], fileVersion, fileVersion2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(fileVersion);
        expect(expectedResult).toContain(fileVersion2);
      });

      it('should accept null and undefined values', () => {
        const fileVersion: IFileVersion = sampleWithRequiredData;
        expectedResult = service.addFileVersionToCollectionIfMissing([], null, fileVersion, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(fileVersion);
      });

      it('should return initial array if no FileVersion is added', () => {
        const fileVersionCollection: IFileVersion[] = [sampleWithRequiredData];
        expectedResult = service.addFileVersionToCollectionIfMissing(fileVersionCollection, undefined, null);
        expect(expectedResult).toEqual(fileVersionCollection);
      });
    });

    describe('compareFileVersion', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareFileVersion(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareFileVersion(entity1, entity2);
        const compareResult2 = service.compareFileVersion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareFileVersion(entity1, entity2);
        const compareResult2 = service.compareFileVersion(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareFileVersion(entity1, entity2);
        const compareResult2 = service.compareFileVersion(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
