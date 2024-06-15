import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { FileVersionDetailComponent } from './file-version-detail.component';

describe('FileVersion Management Detail Component', () => {
  let comp: FileVersionDetailComponent;
  let fixture: ComponentFixture<FileVersionDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileVersionDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: FileVersionDetailComponent,
              resolve: { fileVersion: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(FileVersionDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FileVersionDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load fileVersion on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', FileVersionDetailComponent);

      // THEN
      expect(instance.fileVersion()).toEqual(expect.objectContaining({ id: 123 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
