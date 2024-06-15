import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { IFileVersion } from 'app/entities/file-version/file-version.model';
import { FileVersionService } from 'app/entities/file-version/service/file-version.service';
import { ITag } from 'app/entities/tag/tag.model';
import { TagService } from 'app/entities/tag/service/tag.service';
import { IFolder } from 'app/entities/folder/folder.model';
import { FolderService } from 'app/entities/folder/service/folder.service';
import { IFile } from '../file.model';
import { FileService } from '../service/file.service';
import { FileFormService } from './file-form.service';

import { FileUpdateComponent } from './file-update.component';

describe('File Management Update Component', () => {
  let comp: FileUpdateComponent;
  let fixture: ComponentFixture<FileUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let fileFormService: FileFormService;
  let fileService: FileService;
  let fileVersionService: FileVersionService;
  let tagService: TagService;
  let folderService: FolderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FileUpdateComponent],
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
      .overrideTemplate(FileUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(FileUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    fileFormService = TestBed.inject(FileFormService);
    fileService = TestBed.inject(FileService);
    fileVersionService = TestBed.inject(FileVersionService);
    tagService = TestBed.inject(TagService);
    folderService = TestBed.inject(FolderService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call fileVersion query and add missing value', () => {
      const file: IFile = { id: 456 };
      const fileVersion: IFileVersion = { id: 22646 };
      file.fileVersion = fileVersion;

      const fileVersionCollection: IFileVersion[] = [{ id: 7946 }];
      jest.spyOn(fileVersionService, 'query').mockReturnValue(of(new HttpResponse({ body: fileVersionCollection })));
      const expectedCollection: IFileVersion[] = [fileVersion, ...fileVersionCollection];
      jest.spyOn(fileVersionService, 'addFileVersionToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ file });
      comp.ngOnInit();

      expect(fileVersionService.query).toHaveBeenCalled();
      expect(fileVersionService.addFileVersionToCollectionIfMissing).toHaveBeenCalledWith(fileVersionCollection, fileVersion);
      expect(comp.fileVersionsCollection).toEqual(expectedCollection);
    });

    it('Should call Tag query and add missing value', () => {
      const file: IFile = { id: 456 };
      const tags: ITag[] = [{ id: 12130 }];
      file.tags = tags;

      const tagCollection: ITag[] = [{ id: 24550 }];
      jest.spyOn(tagService, 'query').mockReturnValue(of(new HttpResponse({ body: tagCollection })));
      const additionalTags = [...tags];
      const expectedCollection: ITag[] = [...additionalTags, ...tagCollection];
      jest.spyOn(tagService, 'addTagToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ file });
      comp.ngOnInit();

      expect(tagService.query).toHaveBeenCalled();
      expect(tagService.addTagToCollectionIfMissing).toHaveBeenCalledWith(tagCollection, ...additionalTags.map(expect.objectContaining));
      expect(comp.tagsSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Folder query and add missing value', () => {
      const file: IFile = { id: 456 };
      const folders: IFolder[] = [{ id: 4838 }];
      file.folders = folders;

      const folderCollection: IFolder[] = [{ id: 22611 }];
      jest.spyOn(folderService, 'query').mockReturnValue(of(new HttpResponse({ body: folderCollection })));
      const additionalFolders = [...folders];
      const expectedCollection: IFolder[] = [...additionalFolders, ...folderCollection];
      jest.spyOn(folderService, 'addFolderToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ file });
      comp.ngOnInit();

      expect(folderService.query).toHaveBeenCalled();
      expect(folderService.addFolderToCollectionIfMissing).toHaveBeenCalledWith(
        folderCollection,
        ...additionalFolders.map(expect.objectContaining),
      );
      expect(comp.foldersSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const file: IFile = { id: 456 };
      const fileVersion: IFileVersion = { id: 4855 };
      file.fileVersion = fileVersion;
      const tags: ITag = { id: 5239 };
      file.tags = [tags];
      const folders: IFolder = { id: 12037 };
      file.folders = [folders];

      activatedRoute.data = of({ file });
      comp.ngOnInit();

      expect(comp.fileVersionsCollection).toContain(fileVersion);
      expect(comp.tagsSharedCollection).toContain(tags);
      expect(comp.foldersSharedCollection).toContain(folders);
      expect(comp.file).toEqual(file);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFile>>();
      const file = { id: 123 };
      jest.spyOn(fileFormService, 'getFile').mockReturnValue(file);
      jest.spyOn(fileService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ file });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: file }));
      saveSubject.complete();

      // THEN
      expect(fileFormService.getFile).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(fileService.update).toHaveBeenCalledWith(expect.objectContaining(file));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFile>>();
      const file = { id: 123 };
      jest.spyOn(fileFormService, 'getFile').mockReturnValue({ id: null });
      jest.spyOn(fileService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ file: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: file }));
      saveSubject.complete();

      // THEN
      expect(fileFormService.getFile).toHaveBeenCalled();
      expect(fileService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFile>>();
      const file = { id: 123 };
      jest.spyOn(fileService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ file });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(fileService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareFileVersion', () => {
      it('Should forward to fileVersionService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(fileVersionService, 'compareFileVersion');
        comp.compareFileVersion(entity, entity2);
        expect(fileVersionService.compareFileVersion).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareTag', () => {
      it('Should forward to tagService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(tagService, 'compareTag');
        comp.compareTag(entity, entity2);
        expect(tagService.compareTag).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareFolder', () => {
      it('Should forward to folderService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(folderService, 'compareFolder');
        comp.compareFolder(entity, entity2);
        expect(folderService.compareFolder).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
