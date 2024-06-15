import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../file-version.test-samples';

import { FileVersionFormService } from './file-version-form.service';

describe('FileVersion Form Service', () => {
  let service: FileVersionFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FileVersionFormService);
  });

  describe('Service methods', () => {
    describe('createFileVersionFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createFileVersionFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            fileId: expect.any(Object),
            versionNumber: expect.any(Object),
            objectName: expect.any(Object),
            createdAt: expect.any(Object),
          }),
        );
      });

      it('passing IFileVersion should create a new form with FormGroup', () => {
        const formGroup = service.createFileVersionFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            fileId: expect.any(Object),
            versionNumber: expect.any(Object),
            objectName: expect.any(Object),
            createdAt: expect.any(Object),
          }),
        );
      });
    });

    describe('getFileVersion', () => {
      it('should return NewFileVersion for default FileVersion initial value', () => {
        const formGroup = service.createFileVersionFormGroup(sampleWithNewData);

        const fileVersion = service.getFileVersion(formGroup) as any;

        expect(fileVersion).toMatchObject(sampleWithNewData);
      });

      it('should return NewFileVersion for empty FileVersion initial value', () => {
        const formGroup = service.createFileVersionFormGroup();

        const fileVersion = service.getFileVersion(formGroup) as any;

        expect(fileVersion).toMatchObject({});
      });

      it('should return IFileVersion', () => {
        const formGroup = service.createFileVersionFormGroup(sampleWithRequiredData);

        const fileVersion = service.getFileVersion(formGroup) as any;

        expect(fileVersion).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IFileVersion should not enable id FormControl', () => {
        const formGroup = service.createFileVersionFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewFileVersion should disable id FormControl', () => {
        const formGroup = service.createFileVersionFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
