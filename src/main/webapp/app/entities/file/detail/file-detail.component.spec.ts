import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { FileDetailComponent } from './file-detail.component';

describe('File Management Detail Component', () => {
  let comp: FileDetailComponent;
  let fixture: ComponentFixture<FileDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: FileDetailComponent,
              resolve: { file: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(FileDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FileDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load file on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', FileDetailComponent);

      // THEN
      expect(instance.file()).toEqual(expect.objectContaining({ id: 123 }));
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
