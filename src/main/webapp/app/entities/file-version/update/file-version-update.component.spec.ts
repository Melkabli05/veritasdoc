import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, from } from 'rxjs';

import { FileVersionService } from '../service/file-version.service';
import { IFileVersion } from '../file-version.model';
import { FileVersionFormService } from './file-version-form.service';

import { FileVersionUpdateComponent } from './file-version-update.component';

describe('FileVersion Management Update Component', () => {
  let comp: FileVersionUpdateComponent;
  let fixture: ComponentFixture<FileVersionUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let fileVersionFormService: FileVersionFormService;
  let fileVersionService: FileVersionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FileVersionUpdateComponent],
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
      .overrideTemplate(FileVersionUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(FileVersionUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    fileVersionFormService = TestBed.inject(FileVersionFormService);
    fileVersionService = TestBed.inject(FileVersionService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const fileVersion: IFileVersion = { id: 456 };

      activatedRoute.data = of({ fileVersion });
      comp.ngOnInit();

      expect(comp.fileVersion).toEqual(fileVersion);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFileVersion>>();
      const fileVersion = { id: 123 };
      jest.spyOn(fileVersionFormService, 'getFileVersion').mockReturnValue(fileVersion);
      jest.spyOn(fileVersionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ fileVersion });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: fileVersion }));
      saveSubject.complete();

      // THEN
      expect(fileVersionFormService.getFileVersion).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(fileVersionService.update).toHaveBeenCalledWith(expect.objectContaining(fileVersion));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFileVersion>>();
      const fileVersion = { id: 123 };
      jest.spyOn(fileVersionFormService, 'getFileVersion').mockReturnValue({ id: null });
      jest.spyOn(fileVersionService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ fileVersion: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: fileVersion }));
      saveSubject.complete();

      // THEN
      expect(fileVersionFormService.getFileVersion).toHaveBeenCalled();
      expect(fileVersionService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFileVersion>>();
      const fileVersion = { id: 123 };
      jest.spyOn(fileVersionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ fileVersion });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(fileVersionService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
