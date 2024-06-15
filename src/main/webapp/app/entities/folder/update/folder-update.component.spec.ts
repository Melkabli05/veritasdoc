import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { IFile } from 'app/entities/file/file.model';
import { FileService } from 'app/entities/file/service/file.service';
import { FolderService } from '../service/folder.service';
import { IFolder } from '../folder.model';
import { FolderFormService } from './folder-form.service';

import { FolderUpdateComponent } from './folder-update.component';

describe('Folder Management Update Component', () => {
  let comp: FolderUpdateComponent;
  let fixture: ComponentFixture<FolderUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let folderFormService: FolderFormService;
  let folderService: FolderService;
  let fileService: FileService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FolderUpdateComponent],
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
      .overrideTemplate(FolderUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(FolderUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    folderFormService = TestBed.inject(FolderFormService);
    folderService = TestBed.inject(FolderService);
    fileService = TestBed.inject(FileService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call File query and add missing value', () => {
      const folder: IFolder = { id: 456 };
      const file: IFile = { id: 4788 };
      folder.file = file;
      const files: IFile[] = [{ id: 5715 }];
      folder.files = files;

      const fileCollection: IFile[] = [{ id: 22494 }];
      jest.spyOn(fileService, 'query').mockReturnValue(of(new HttpResponse({ body: fileCollection })));
      const additionalFiles = [file, ...files];
      const expectedCollection: IFile[] = [...additionalFiles, ...fileCollection];
      jest.spyOn(fileService, 'addFileToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ folder });
      comp.ngOnInit();

      expect(fileService.query).toHaveBeenCalled();
      expect(fileService.addFileToCollectionIfMissing).toHaveBeenCalledWith(
        fileCollection,
        ...additionalFiles.map(expect.objectContaining),
      );
      expect(comp.filesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const folder: IFolder = { id: 456 };
      const file: IFile = { id: 24135 };
      folder.file = file;
      const files: IFile = { id: 19880 };
      folder.files = [files];

      activatedRoute.data = of({ folder });
      comp.ngOnInit();

      expect(comp.filesSharedCollection).toContain(file);
      expect(comp.filesSharedCollection).toContain(files);
      expect(comp.folder).toEqual(folder);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFolder>>();
      const folder = { id: 123 };
      jest.spyOn(folderFormService, 'getFolder').mockReturnValue(folder);
      jest.spyOn(folderService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ folder });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: folder }));
      saveSubject.complete();

      // THEN
      expect(folderFormService.getFolder).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(folderService.update).toHaveBeenCalledWith(expect.objectContaining(folder));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFolder>>();
      const folder = { id: 123 };
      jest.spyOn(folderFormService, 'getFolder').mockReturnValue({ id: null });
      jest.spyOn(folderService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ folder: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: folder }));
      saveSubject.complete();

      // THEN
      expect(folderFormService.getFolder).toHaveBeenCalled();
      expect(folderService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFolder>>();
      const folder = { id: 123 };
      jest.spyOn(folderService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ folder });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(folderService.update).toHaveBeenCalled();
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
