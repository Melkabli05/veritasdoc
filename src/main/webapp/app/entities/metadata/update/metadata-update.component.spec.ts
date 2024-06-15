import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { IFile } from 'app/entities/file/file.model';
import { FileService } from 'app/entities/file/service/file.service';
import { MetadataService } from '../service/metadata.service';
import { IMetadata } from '../metadata.model';
import { MetadataFormService } from './metadata-form.service';

import { MetadataUpdateComponent } from './metadata-update.component';

describe('Metadata Management Update Component', () => {
  let comp: MetadataUpdateComponent;
  let fixture: ComponentFixture<MetadataUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let metadataFormService: MetadataFormService;
  let metadataService: MetadataService;
  let fileService: FileService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MetadataUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(MetadataUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(MetadataUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    metadataFormService = TestBed.inject(MetadataFormService);
    metadataService = TestBed.inject(MetadataService);
    fileService = TestBed.inject(FileService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call File query and add missing value', () => {
      const metadata: IMetadata = { id: 456 };
      const file: IFile = { id: 13818 };
      metadata.file = file;

      const fileCollection: IFile[] = [{ id: 20091 }];
      jest.spyOn(fileService, 'query').mockReturnValue(of(new HttpResponse({ body: fileCollection })));
      const additionalFiles = [file];
      const expectedCollection: IFile[] = [...additionalFiles, ...fileCollection];
      jest.spyOn(fileService, 'addFileToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ metadata });
      comp.ngOnInit();

      expect(fileService.query).toHaveBeenCalled();
      expect(fileService.addFileToCollectionIfMissing).toHaveBeenCalledWith(
        fileCollection,
        ...additionalFiles.map(expect.objectContaining),
      );
      expect(comp.filesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const metadata: IMetadata = { id: 456 };
      const file: IFile = { id: 25457 };
      metadata.file = file;

      activatedRoute.data = of({ metadata });
      comp.ngOnInit();

      expect(comp.filesSharedCollection).toContain(file);
      expect(comp.metadata).toEqual(metadata);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMetadata>>();
      const metadata = { id: 123 };
      jest.spyOn(metadataFormService, 'getMetadata').mockReturnValue(metadata);
      jest.spyOn(metadataService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ metadata });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: metadata }));
      saveSubject.complete();

      // THEN
      expect(metadataFormService.getMetadata).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(metadataService.update).toHaveBeenCalledWith(expect.objectContaining(metadata));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMetadata>>();
      const metadata = { id: 123 };
      jest.spyOn(metadataFormService, 'getMetadata').mockReturnValue({ id: null });
      jest.spyOn(metadataService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ metadata: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: metadata }));
      saveSubject.complete();

      // THEN
      expect(metadataFormService.getMetadata).toHaveBeenCalled();
      expect(metadataService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMetadata>>();
      const metadata = { id: 123 };
      jest.spyOn(metadataService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ metadata });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(metadataService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareFile', () => {
      it('Should forward to fileService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(fileService, 'compareFile');
        comp.compareFile(entity, entity2);
        expect(fileService.compareFile).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
