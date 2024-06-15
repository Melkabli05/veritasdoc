import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { IFile } from 'app/entities/file/file.model';
import { FileService } from 'app/entities/file/service/file.service';
import { TagService } from '../service/tag.service';
import { ITag } from '../tag.model';
import { TagFormService } from './tag-form.service';

import { TagUpdateComponent } from './tag-update.component';

describe('Tag Management Update Component', () => {
  let comp: TagUpdateComponent;
  let fixture: ComponentFixture<TagUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let tagFormService: TagFormService;
  let tagService: TagService;
  let fileService: FileService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, TagUpdateComponent],
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
      .overrideTemplate(TagUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(TagUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    tagFormService = TestBed.inject(TagFormService);
    tagService = TestBed.inject(TagService);
    fileService = TestBed.inject(FileService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call File query and add missing value', () => {
      const tag: ITag = { id: 456 };
      const file: IFile = { id: 25158 };
      tag.file = file;
      const files: IFile[] = [{ id: 17989 }];
      tag.files = files;

      const fileCollection: IFile[] = [{ id: 12149 }];
      jest.spyOn(fileService, 'query').mockReturnValue(of(new HttpResponse({ body: fileCollection })));
      const additionalFiles = [file, ...files];
      const expectedCollection: IFile[] = [...additionalFiles, ...fileCollection];
      jest.spyOn(fileService, 'addFileToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ tag });
      comp.ngOnInit();

      expect(fileService.query).toHaveBeenCalled();
      expect(fileService.addFileToCollectionIfMissing).toHaveBeenCalledWith(
        fileCollection,
        ...additionalFiles.map(expect.objectContaining),
      );
      expect(comp.filesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const tag: ITag = { id: 456 };
      const file: IFile = { id: 18089 };
      tag.file = file;
      const files: IFile = { id: 7210 };
      tag.files = [files];

      activatedRoute.data = of({ tag });
      comp.ngOnInit();

      expect(comp.filesSharedCollection).toContain(file);
      expect(comp.filesSharedCollection).toContain(files);
      expect(comp.tag).toEqual(tag);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITag>>();
      const tag = { id: 123 };
      jest.spyOn(tagFormService, 'getTag').mockReturnValue(tag);
      jest.spyOn(tagService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ tag });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: tag }));
      saveSubject.complete();

      // THEN
      expect(tagFormService.getTag).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(tagService.update).toHaveBeenCalledWith(expect.objectContaining(tag));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITag>>();
      const tag = { id: 123 };
      jest.spyOn(tagFormService, 'getTag').mockReturnValue({ id: null });
      jest.spyOn(tagService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ tag: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: tag }));
      saveSubject.complete();

      // THEN
      expect(tagFormService.getTag).toHaveBeenCalled();
      expect(tagService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ITag>>();
      const tag = { id: 123 };
      jest.spyOn(tagService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ tag });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(tagService.update).toHaveBeenCalled();
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
